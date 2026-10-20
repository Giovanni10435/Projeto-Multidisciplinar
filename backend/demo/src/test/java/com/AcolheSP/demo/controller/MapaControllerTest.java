package com.AcolheSP.demo.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoMapaResponse;
import com.AcolheSP.demo.service.MapaService;

@ExtendWith(MockitoExtension.class)
class MapaControllerTest {

    @Mock
    private MapaService mapaService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new MapaController(mapaService)).build();
    }

    private AbrigoMapaResponse abrigo(Double distancia) {
        return new AbrigoMapaResponse(1, "Casa Luz", "Rua A, 10",
                BigDecimal.valueOf(-23.55), BigDecimal.valueOf(-46.63), "PENDENTE", 2L, distancia);
    }

    @Test
    void deveRepassarLatELngDoUsuario() throws Exception {
        when(mapaService.listarParaMapa(-23.55, -46.63)).thenReturn(List.of(abrigo(1.5)));

        mvc.perform(get("/api/local/abrigos").param("lat", "-23.55").param("lng", "-46.63"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Casa Luz"))
                .andExpect(jsonPath("$[0].necessidadesAbertas").value(2))
                .andExpect(jsonPath("$[0].distanciaKm").value(1.5));
    }

    @Test
    void semParametrosDeveChamarComNulos() throws Exception {
        when(mapaService.listarParaMapa(null, null)).thenReturn(List.of(abrigo(null)));

        mvc.perform(get("/api/local/abrigos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanciaKm").doesNotExist());
    }

    @Test
    void deveRetornar400QuandoServiceRejeitarCoordenadas() throws Exception {
        when(mapaService.listarParaMapa(-23.55, null))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie lat e lng juntas"));

        mvc.perform(get("/api/local/abrigos").param("lat", "-23.55"))
                .andExpect(status().isBadRequest());
    }
}