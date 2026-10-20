package com.AcolheSP.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoDetalheResponse;
import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.service.AbrigoService;

@ExtendWith(MockitoExtension.class)
class AbrigoControllerTest {

    @Mock
    private AbrigoService abrigoService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new AbrigoController(abrigoService)).build();
    }

    private AbrigoResponse resposta(int id, String nome) {
        return new AbrigoResponse(id, nome, null, "Acolhimento", "(11) 3333-4444", "contato@exemplo.com",
                "PENDENTE", "Rua A, 10", "01234-567",
                BigDecimal.valueOf(-23.55), BigDecimal.valueOf(-46.63));
    }

    @Test
    void cadastrarDeveRetornar201ComLocation() throws Exception {
        when(abrigoService.cadastrarAbrigo(any(AbrigoRequest.class))).thenReturn(resposta(7, "Casa Esperança"));

        mvc.perform(post("/api/abrigos").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"a@a.com","nome":"Casa Esperança","email_contato":"contato@exemplo.com",
                 "endereco":"Rua A, 10","latitude":-23.55,"longitude":-46.63}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/abrigos/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Casa Esperança"));
    }

    @Test
    void cadastrarDeveRetornar409QuandoServiceRejeitar() throws Exception {
        when(abrigoService.cadastrarAbrigo(any(AbrigoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com esse email"));

        mvc.perform(post("/api/abrigos").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@a.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void listarDeveRepassarFiltros() throws Exception {
        when(abrigoService.listarAbrigos("casa", "cobertor")).thenReturn(List.of(resposta(1, "Casa Luz")));

        mvc.perform(get("/api/abrigos").param("nome", "casa").param("necessidade", "cobertor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Casa Luz"));
    }

    @Test
    void listarSemFiltrosDeveRepassarNulos() throws Exception {
        when(abrigoService.listarAbrigos(null, null)).thenReturn(List.of());

        mvc.perform(get("/api/abrigos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void detalharDeveRetornarAbrigoComNecessidades() throws Exception {
        when(abrigoService.detalharAbrigo(7))
                .thenReturn(new AbrigoDetalheResponse(resposta(7, "Casa Luz"), List.of()));

        mvc.perform(get("/api/abrigos/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abrigo.id").value(7))
                .andExpect(jsonPath("$.necessidades").isEmpty());
    }

    @Test
    void detalharDeveRetornar404() throws Exception {
        when(abrigoService.detalharAbrigo(99))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));

        mvc.perform(get("/api/abrigos/99")).andExpect(status().isNotFound());
    }

    @Test
    void atualizarDeveRetornar200() throws Exception {
        when(abrigoService.atualizarAbrigo(eq(7), any(AbrigoRequest.class))).thenReturn(resposta(7, "Nome novo"));

        mvc.perform(put("/api/abrigos/7").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Nome novo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome novo"));
    }
}