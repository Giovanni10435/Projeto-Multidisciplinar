package com.AcolheSP.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.AcolheSP.demo.dto.SolicitacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.service.SolicitacaoService;

@ExtendWith(MockitoExtension.class)
class SolicitacaoControllerTest {

    @Mock
    private SolicitacaoService solicitacaoService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new SolicitacaoController(solicitacaoService)).build();
    }

    private SolicitacaoResponse doacao(int id, String status) {
        return new SolicitacaoResponse(id, "DOACAO", status, null, 5, "Ana", "(11) 99999-0000", "Sábados",
                7, "Cobertores", 3, null, null);
    }

    @Test
    void listarDeveRetornarSolicitacoesDoAbrigo() throws Exception {
        when(solicitacaoService.listar(1)).thenReturn(List.of(doacao(3, "PENDENTE")));

        mvc.perform(get("/api/instituicoes/1/solicitacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].tipo").value("DOACAO"))
                .andExpect(jsonPath("$[0].voluntarioNome").value("Ana"));
    }

    @Test
    void listarDeveRetornar404ParaAbrigoInexistente() throws Exception {
        when(solicitacaoService.listar(99))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));

        mvc.perform(get("/api/instituicoes/99/solicitacoes")).andExpect(status().isNotFound());
    }

    @Test
    void atualizarDeveRepassarTipoEStatus() throws Exception {
        when(solicitacaoService.atualizar(eq(1), eq(3), any(SolicitacaoRequest.class)))
                .thenReturn(doacao(3, "CONFIRMADA"));

        mvc.perform(put("/api/instituicoes/1/solicitacoes/3").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"DOACAO\",\"status\":\"APROVADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));

        ArgumentCaptor<SolicitacaoRequest> captor = ArgumentCaptor.forClass(SolicitacaoRequest.class);
        verify(solicitacaoService).atualizar(eq(1), eq(3), captor.capture());
        assertEquals("DOACAO", captor.getValue().tipo());
        assertEquals("APROVADA", captor.getValue().status());
    }

    @Test
    void atualizarDeveRetornar409QuandoStatusJaEhFinal() throws Exception {
        when(solicitacaoService.atualizar(eq(1), eq(3), any(SolicitacaoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Doação já está ENTREGUE"));

        mvc.perform(put("/api/instituicoes/1/solicitacoes/3").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"DOACAO\",\"status\":\"APROVADA\"}"))
                .andExpect(status().isConflict());
    }
}