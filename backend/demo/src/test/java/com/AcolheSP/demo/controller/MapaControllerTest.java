package com.AcolheSP.demo.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.math.BigDecimal;
import java.util.UUID;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.service.AbrigoService;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MapaControllerTest {


    @Autowired
    private MockMvc mvc;


    @Autowired
    private AbrigoService abrigoService;


    private static String token() {
        return UUID.randomUUID().toString().substring(0, 8);
    }


    private static String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID() + "@exemplo.com";
    }


    // Praça da Sé (SP) e Campinas
    private static final String SP_LAT = "-23.5505";
    private static final String SP_LNG = "-46.6333";
    private static final String CAMP_LAT = "-22.9099";
    private static final String CAMP_LNG = "-47.0626";


    private AbrigoResponse criarAbrigoNoMapa(String nome, String lat, String lng) {
        return abrigoService.cadastrarAbrigo(new AbrigoRequest(
                emailUnico("abrigo"),
                nome,
                null,
                null,
                null,
                null,
                "Rua " + nome,
                null,
                new BigDecimal(lat),
                new BigDecimal(lng)
        ));
    }


    @Test
    void semParametrosDeveListarAbrigosSemDistancia() throws Exception {
        String t = token();


        criarAbrigoNoMapa("Controller Beta " + t, SP_LAT, SP_LNG);
        criarAbrigoNoMapa("Controller Alfa " + t, SP_LAT, SP_LNG);


        mvc.perform(get("/api/local/abrigos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Controller Alfa " + t + "')].distanciaKm")
                        .value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.nullValue())))
                .andExpect(jsonPath("$[?(@.nome == 'Controller Beta " + t + "')].distanciaKm")
                        .value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.nullValue())));
    }


    @Test
    void comCoordenadasDeveRetornarAbrigosOrdenadosPorProximidade() throws Exception {
        String t = token();


        criarAbrigoNoMapa("Controller Longe " + t, CAMP_LAT, CAMP_LNG);
        criarAbrigoNoMapa("Controller Perto " + t, SP_LAT, SP_LNG);


        mvc.perform(get("/api/local/abrigos")
                        .param("lat", SP_LAT)
                        .param("lng", SP_LNG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Controller Perto " + t + "')].distanciaKm")
                        .value(org.hamcrest.Matchers.hasItem(0.0)))
                .andExpect(jsonPath("$[?(@.nome == 'Controller Longe " + t + "')].distanciaKm")
                        .value(org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.greaterThan(80.0))));
    }


    @Test
    void deveRetornar400QuandoEnviarLatSemLng() throws Exception {
        mvc.perform(get("/api/local/abrigos")
                        .param("lat", SP_LAT))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deveRetornar400QuandoEnviarLngSemLat() throws Exception {
        mvc.perform(get("/api/local/abrigos")
                        .param("lng", SP_LNG))
                .andExpect(status().isBadRequest());
    }
}