package com.AcolheSP.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.NecessidadeRequest;
import com.AcolheSP.demo.dto.NecessidadeResponse;
import com.AcolheSP.demo.service.NecessidadeService;

@ExtendWith(MockitoExtension.class)
class NecessidadeControllerTest {

    @Mock
    private NecessidadeService necessidadeService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new NecessidadeController(necessidadeService)).build();
    }

    private NecessidadeResponse resposta() {
        return new NecessidadeResponse(7, "Cobertores", 50, 0, "ABERTA", 1);
    }

    @Test
    void cadastrarDeveRetornar201ComLocationEAceitarSnakeCase() throws Exception {
        when(necessidadeService.cadastrarNecessidade(eq(1), any(NecessidadeRequest.class))).thenReturn(resposta());

        mvc.perform(post("/api/instituicoes/1/necessidades").contentType(MediaType.APPLICATION_JSON)
                .content("{\"descricao\":\"Cobertores\",\"quantidade_solicitada\":50}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/instituicoes/1/necessidades/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("ABERTA"));

        ArgumentCaptor<NecessidadeRequest> captor = ArgumentCaptor.forClass(NecessidadeRequest.class);
        verify(necessidadeService).cadastrarNecessidade(eq(1), captor.capture());
        assertEquals(50, captor.getValue().quantidadeSolicitada());
    }

    @Test
    void cadastrarDeveRetornar400QuandoServiceRejeitar() throws Exception {
        when(necessidadeService.cadastrarNecessidade(eq(1), any(NecessidadeRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: descricao"));

        mvc.perform(post("/api/instituicoes/1/necessidades").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarDeveRetornar200() throws Exception {
        when(necessidadeService.atualizarNecessidade(eq(1), eq(7), any(NecessidadeRequest.class)))
                .thenReturn(resposta());

        mvc.perform(put("/api/instituicoes/1/necessidades/7").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ATENDIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void atualizarDeveRetornar404() throws Exception {
        when(necessidadeService.atualizarNecessidade(eq(1), eq(99), any(NecessidadeRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Necessidade não encontrada nesse abrigo"));

        mvc.perform(put("/api/instituicoes/1/necessidades/99").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removerDeveRetornar204() throws Exception {
        mvc.perform(delete("/api/instituicoes/1/necessidades/7")).andExpect(status().isNoContent());

        verify(necessidadeService).removerNecessidade(1, 7);
    }

    @Test
    void listarDeveRetornarNecessidadesDoAbrigo() throws Exception {
        when(necessidadeService.listarNecessidades(1)).thenReturn(List.of(resposta()));

        mvc.perform(get("/api/instituicoes/1/necessidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Cobertores"));
    }
}