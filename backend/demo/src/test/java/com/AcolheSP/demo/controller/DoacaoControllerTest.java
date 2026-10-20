package com.AcolheSP.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.AcolheSP.demo.dto.DoacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.service.DoacaoService;

@ExtendWith(MockitoExtension.class)
class DoacaoControllerTest {

    @Mock
    private DoacaoService doacaoService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DoacaoController(doacaoService)).build();
    }

    private static final String BODY = "{\"voluntario_id\":5,\"necessidade_id\":7,\"quantidade\":3}";

    @Test
    void deveRetornar201ComLocationEAceitarSnakeCase() throws Exception {
        when(doacaoService.realizarDoacao(any(DoacaoRequest.class))).thenReturn(new SolicitacaoResponse(
                9, "DOACAO", "PENDENTE", null, 5, "Ana", null, null, 7, "Cobertores", 3, null, null));

        mvc.perform(post("/api/doacoes").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/doacoes/9"))
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        ArgumentCaptor<DoacaoRequest> captor = ArgumentCaptor.forClass(DoacaoRequest.class);
        verify(doacaoService).realizarDoacao(captor.capture());
        assertEquals(5, captor.getValue().voluntarioId());
        assertEquals(7, captor.getValue().necessidadeId());
        assertEquals(3, captor.getValue().quantidade());
    }

    @Test
    void deveRetornar409QuandoNecessidadeNaoEstaAberta() throws Exception {
        when(doacaoService.realizarDoacao(any(DoacaoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Essa necessidade não está mais aberta"));

        mvc.perform(post("/api/doacoes").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar404QuandoVoluntarioNaoExiste() throws Exception {
        when(doacaoService.realizarDoacao(any(DoacaoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Voluntário não encontrado"));

        mvc.perform(post("/api/doacoes").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }
}