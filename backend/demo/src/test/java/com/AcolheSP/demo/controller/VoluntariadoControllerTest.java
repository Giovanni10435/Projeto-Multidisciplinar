package com.AcolheSP.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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

import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.dto.VoluntariadoRequest;
import com.AcolheSP.demo.service.VoluntariadoService;

@ExtendWith(MockitoExtension.class)
class VoluntariadoControllerTest {

    @Mock
    private VoluntariadoService voluntariadoService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new VoluntariadoController(voluntariadoService)).build();
    }

    private static final String BODY = "{\"voluntario_id\":5,\"data_inicio\":\"2026-11-01\",\"data_fim\":\"2026-12-01\"}";

    @Test
    void deveRetornar201ComLocationEAceitarSnakeCaseEDatas() throws Exception {
        when(voluntariadoService.demonstrarInteresse(eq(1), any(VoluntariadoRequest.class)))
                .thenReturn(new SolicitacaoResponse(4, "VOLUNTARIADO", "PENDENTE", null, 5, "Ana", null, null,
                        null, null, null, null, null));

        mvc.perform(post("/api/instituicoes/1/voluntariados").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/instituicoes/1/voluntariados/4"))
                .andExpect(jsonPath("$.tipo").value("VOLUNTARIADO"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        ArgumentCaptor<VoluntariadoRequest> captor = ArgumentCaptor.forClass(VoluntariadoRequest.class);
        verify(voluntariadoService).demonstrarInteresse(eq(1), captor.capture());
        assertEquals(5, captor.getValue().voluntarioId());
        assertEquals(LocalDate.of(2026, 11, 1), captor.getValue().dataInicio());
        assertEquals(LocalDate.of(2026, 12, 1), captor.getValue().dataFim());
    }

    @Test
    void deveRetornar409QuandoJaTemCandidaturaAtiva() throws Exception {
        when(voluntariadoService.demonstrarInteresse(eq(1), any(VoluntariadoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                        "Esse voluntário já tem uma candidatura ativa nesse abrigo"));

        mvc.perform(post("/api/instituicoes/1/voluntariados").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar404ParaAbrigoInexistente() throws Exception {
        when(voluntariadoService.demonstrarInteresse(eq(99), any(VoluntariadoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));

        mvc.perform(post("/api/instituicoes/99/voluntariados").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }
}