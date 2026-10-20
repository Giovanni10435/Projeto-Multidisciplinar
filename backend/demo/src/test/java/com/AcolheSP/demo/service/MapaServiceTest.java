package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Localizacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.NecessidadeRepository;

@ExtendWith(MockitoExtension.class)
class MapaServiceTest {

    // Praça da Sé (SP) e Campinas
    private static final String SP_LAT = "-23.5505";
    private static final String SP_LNG = "-46.6333";
    private static final String CAMP_LAT = "-22.9099";
    private static final String CAMP_LNG = "-47.0626";

    @Mock
    private InstituicaoRepository instituicaoRepo;

    @Mock
    private NecessidadeRepository necessidadeRepo;

    private MapaService service;

    @BeforeEach
    void setUp() {
        service = new MapaService(instituicaoRepo, necessidadeRepo);
    }

    private Instituicao abrigo(int id, String nome, String lat, String lng) {
        Instituicao i = new Instituicao();
        i.setId(id);
        i.setNome(nome);
        Localizacao loc = new Localizacao();
        loc.setEndereco("Rua " + nome);
        loc.setLatitude(new BigDecimal(lat));
        loc.setLongitude(new BigDecimal(lng));
        i.setLocalizacao(loc);
        return i;
    }

    private void abertas(int instituicaoId, long total) {
        when(necessidadeRepo.countByInstituicaoIdAndStatus(instituicaoId, StatusNecessidade.ABERTA))
                .thenReturn(total);
    }

    @Test
    void naoDeveAceitarLatSemLng() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listarParaMapa(-23.5, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Envie lat e lng juntas", ex.getReason());
    }

    @Test
    void naoDeveAceitarLngSemLat() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listarParaMapa(null, -46.6));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveAceitarCoordenadasForaDoIntervalo() {
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.listarParaMapa(91.0, 0.0)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.listarParaMapa(-91.0, 0.0)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.listarParaMapa(0.0, 181.0)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.listarParaMapa(0.0, -181.0)).getStatusCode());
    }

    @Test
    void semCoordenadasDeveOrdenarPorNomeIgnorandoMaiusculaEDeixarDistanciaNula() {
        when(instituicaoRepo.findAll()).thenReturn(List.of(
                abrigo(1, "beta", SP_LAT, SP_LNG),
                abrigo(2, "Alfa", SP_LAT, SP_LNG),
                abrigo(3, "Casa", SP_LAT, SP_LNG)));
        abertas(1, 0);
        abertas(2, 3);
        abertas(3, 1);

        List<AbrigoMapaResponse> res = service.listarParaMapa(null, null);

        assertEquals(List.of("Alfa", "beta", "Casa"), res.stream().map(AbrigoMapaResponse::nome).toList());
        assertNull(res.get(0).distanciaKm());
        assertEquals(3L, res.get(0).necessidadesAbertas());
        assertEquals("PENDENTE", res.get(0).statusVerificacao());
    }

    @Test
    void deveIgnorarAbrigoSemLocalizacao() {
        Instituicao semLocal = new Instituicao();
        semLocal.setId(9);
        semLocal.setNome("Sem local");
        when(instituicaoRepo.findAll()).thenReturn(List.of(semLocal, abrigo(1, "Com local", SP_LAT, SP_LNG)));
        abertas(1, 0);

        List<AbrigoMapaResponse> res = service.listarParaMapa(null, null);

        assertEquals(1, res.size());
        assertEquals("Com local", res.get(0).nome());
    }

    @Test
    void comCoordenadasDeveOrdenarDoMaisPertoParaOMaisLongeComDistancia() {
        when(instituicaoRepo.findAll()).thenReturn(List.of(
                abrigo(2, "Longe", CAMP_LAT, CAMP_LNG),
                abrigo(1, "Perto", SP_LAT, SP_LNG)));
        abertas(1, 0);
        abertas(2, 0);

        List<AbrigoMapaResponse> res = service.listarParaMapa(Double.valueOf(SP_LAT), Double.valueOf(SP_LNG));

        assertEquals("Perto", res.get(0).nome());
        assertEquals(0.0, res.get(0).distanciaKm(), 0.001);
        assertEquals("Longe", res.get(1).nome());
        assertTrue(res.get(1).distanciaKm() > 80 && res.get(1).distanciaKm() < 95);
    }

    @Test
    void deveRetornarListaVaziaSemAbrigos() {
        when(instituicaoRepo.findAll()).thenReturn(List.of());

        assertTrue(service.listarParaMapa(null, null).isEmpty());
    }

    @Test
    void haversineDeveCalcularDistanciaEntreSaoPauloERio() {
        double km = MapaService.haversineKm(-23.5505, -46.6333, -22.9068, -43.1729);
        assertEquals(357.0, km, 5.0);
    }

    @Test
    void haversineDeveRetornarZeroParaOMesmoPonto() {
        assertEquals(0.0, MapaService.haversineKm(-23.55, -46.63, -23.55, -46.63), 0.0001);
    }
}