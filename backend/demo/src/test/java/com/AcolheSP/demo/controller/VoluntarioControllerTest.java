package com.AcolheSP.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.AcolheSP.demo.dto.VoluntarioRequest;
import com.AcolheSP.demo.dto.VoluntarioResponse;
import com.AcolheSP.demo.service.VoluntarioService;

@ExtendWith(MockitoExtension.class)
class VoluntarioControllerTest {

    @Mock
    private VoluntarioService voluntarioService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new VoluntarioController(voluntarioService)).build();
    }

    @Test
    void cadastrarDeveRetornar201ComLocationERepassarSenha() throws Exception {
        when(voluntarioService.cadastrar(any(VoluntarioRequest.class)))
                .thenReturn(new VoluntarioResponse(10, "ana@exemplo.com", "Ana", null, null));

        mvc.perform(post("/api/voluntarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@exemplo.com\",\"nome\":\"Ana\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/voluntarios/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        ArgumentCaptor<VoluntarioRequest> captor = ArgumentCaptor.forClass(VoluntarioRequest.class);
        verify(voluntarioService).cadastrar(captor.capture());
        assertEquals("senha123", captor.getValue().senha());
        assertEquals("ana@exemplo.com", captor.getValue().email());
    }

    @Test
    void cadastrarDeveRetornar409QuandoEmailJaExiste() throws Exception {
        when(voluntarioService.cadastrar(any(VoluntarioRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com esse email"));

        mvc.perform(post("/api/voluntarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@exemplo.com\",\"nome\":\"Ana\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void buscarDeveRetornar200() throws Exception {
        when(voluntarioService.buscar(10))
                .thenReturn(new VoluntarioResponse(10, "ana@exemplo.com", "Ana", "(11) 99999-0000", "Sábados"));

        mvc.perform(get("/api/voluntarios/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.disponibilidade").value("Sábados"));
    }

    @Test
    void buscarDeveRetornar404() throws Exception {
        when(voluntarioService.buscar(99))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Voluntário não encontrado"));

        mvc.perform(get("/api/voluntarios/99")).andExpect(status().isNotFound());
    }
}