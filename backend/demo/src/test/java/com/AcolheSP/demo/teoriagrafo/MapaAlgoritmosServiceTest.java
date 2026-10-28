package com.AcolheSP.demo.teoriagrafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoMapaResponse;
import com.AcolheSP.demo.dto.MapaAlgoritmosResponse;
import com.AcolheSP.demo.service.MapaAlgoritmosService;
import com.AcolheSP.demo.service.MapaService;

@ExtendWith(MockitoExtension.class)
class MapaAlgoritmosServiceTest {
    @Mock private MapaService mapaService;
    private MapaAlgoritmosService service;

    @BeforeEach
    void setUp() { service = new MapaAlgoritmosService(mapaService); }

    @Test
    void validaCoordenadasObrigatorias() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.calcularAlgoritmos(null, -46.6));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void retornaHaversineDijkstraEColoracaoComAbrigos() {
        List<AbrigoMapaResponse> abrigos = List.of(
                abrigo(1, "A", -23.5500, -46.6300, 0.3),
                abrigo(2, "B", -23.5510, -46.6310, 0.4),
                abrigo(3, "C", -23.5520, -46.6320, 0.5),
                abrigo(4, "D", -23.5530, -46.6330, 0.6));
        when(mapaService.listarParaMapa(-23.55, -46.63)).thenReturn(abrigos);

        MapaAlgoritmosResponse resposta = service.calcularAlgoritmos(-23.55, -46.63);

        assertEquals(4, resposta.porHaversine().size());
        assertEquals(4, resposta.porDijkstra().size());
        assertFalse(resposta.porColoracao().isEmpty());
        assertTrue(resposta.descricaoGrafo().contains("não representa uma rota por ruas"));
        assertNotNull(resposta.porDijkstra().get(0).distanciaGrafoKm());
        assertTrue(resposta.porDijkstra().stream().allMatch(item -> !item.caminhoIds().isEmpty()));
    }

    @Test
    void naoFalhaQuandoNaoHaAbrigosNoMapa() {
        when(mapaService.listarParaMapa(0.0, 0.0)).thenReturn(List.of());
        MapaAlgoritmosResponse resposta = service.calcularAlgoritmos(0.0, 0.0);
        assertTrue(resposta.porHaversine().isEmpty());
        assertTrue(resposta.porDijkstra().isEmpty());
        assertTrue(resposta.porColoracao().isEmpty());
    }

    private static AbrigoMapaResponse abrigo(int id, String nome, double lat, double lng, double distancia) {
        return new AbrigoMapaResponse(id, nome, "Rua " + nome, BigDecimal.valueOf(lat), BigDecimal.valueOf(lng),
                "VERIFICADA", 0L, distancia);
    }
}
