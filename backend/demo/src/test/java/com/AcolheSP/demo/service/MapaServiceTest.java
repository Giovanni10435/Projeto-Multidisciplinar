package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoMapaResponse;
import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusNecessidade;

@SpringBootTest
@Transactional
class MapaServiceTest {

    @Autowired
    private EntityManager em;

    private static String token() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID() + "@exemplo.com";
    }

    // Abrigo sem localização (para testes que não precisam de mapa)
    private Instituicao criarInstituicao(String nome) {
        Instituicao i = new Instituicao();

        i.setNome(nome);
        
        em.persist(i);
        em.flush();
        return i;
    }

    private Necessidade criarNecessidade(Instituicao inst, String descricao, int solicitada, int recebida,
            StatusNecessidade status) {
        Necessidade n = new Necessidade();
        n.setInstituicao(inst);
        n.setDescricao(descricao);
        n.setQuantidadeSolicitada(solicitada);
        n.setQuantidadeRecebida(recebida);
        n.setStatus(status);
        em.persist(n);
        em.flush();
        return n;
    }

    private void sincronizar() {
        em.flush();
        em.clear();
    }

    // Praça da Sé (SP) e Campinas
    private static final String SP_LAT = "-23.5505";
    private static final String SP_LNG = "-46.6333";
    private static final String CAMP_LAT = "-22.9099";
    private static final String CAMP_LNG = "-47.0626";

    @Autowired
    private MapaService service;

    @Autowired
    private AbrigoService abrigoService;

    private AbrigoResponse criarAbrigoNoMapa(String nome, String lat, String lng) {
        return abrigoService.cadastrarAbrigo(new AbrigoRequest(emailUnico("abrigo"), nome, null, null, null, null,
                "Rua " + nome, null, new BigDecimal(lat), new BigDecimal(lng)));
    }

    // O banco já tem outros abrigos: olhamos só os que este teste criou (nome com o token)
    private List<AbrigoMapaResponse> somenteOsMeus(List<AbrigoMapaResponse> todos, String token) {
        return todos.stream().filter(a -> a.nome().contains(token)).toList();
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
    void semCoordenadasDeveOrdenarPorNomeContarNecessidadesAbertasEDeixarDistanciaNula() {
        String t = token();
        AbrigoResponse beta = criarAbrigoNoMapa("beta " + t, SP_LAT, SP_LNG);
        AbrigoResponse alfa = criarAbrigoNoMapa("Alfa " + t, SP_LAT, SP_LNG);
        Instituicao alfaEntidade = em.find(Instituicao.class, alfa.id());
        criarNecessidade(alfaEntidade, "Cobertores", 10, 0, StatusNecessidade.ABERTA);
        criarNecessidade(alfaEntidade, "Roupas", 10, 0, StatusNecessidade.ABERTA);
        criarNecessidade(alfaEntidade, "Alimentos", 10, 10, StatusNecessidade.ATENDIDA);
        sincronizar();

        List<AbrigoMapaResponse> meus = somenteOsMeus(service.listarParaMapa(null, null), t);

        assertEquals(List.of("Alfa " + t, "beta " + t), meus.stream().map(AbrigoMapaResponse::nome).toList());
        assertNull(meus.get(0).distanciaKm());
        assertEquals(2L, meus.get(0).necessidadesAbertas());
        assertEquals(0L, meus.get(1).necessidadesAbertas());
        assertEquals("PENDENTE", meus.get(0).statusVerificacao());
        assertEquals(beta.id(), meus.get(1).id());
    }

    @Test
    void deveIgnorarAbrigoSemLocalizacao() {
        String t = token();
        criarInstituicao("Sem local " + t);
        criarAbrigoNoMapa("Com local " + t, SP_LAT, SP_LNG);
        sincronizar();

        List<AbrigoMapaResponse> meus = somenteOsMeus(service.listarParaMapa(null, null), t);

        assertEquals(1, meus.size());
        assertEquals("Com local " + t, meus.get(0).nome());
    }

    @Test
    void comCoordenadasDeveOrdenarDoMaisPertoParaOMaisLongeComDistancia() {
        String t = token();
        criarAbrigoNoMapa("Longe " + t, CAMP_LAT, CAMP_LNG);
        criarAbrigoNoMapa("Perto " + t, SP_LAT, SP_LNG);
        sincronizar();

        List<AbrigoMapaResponse> meus = somenteOsMeus(
                service.listarParaMapa(Double.valueOf(SP_LAT), Double.valueOf(SP_LNG)), t);

        assertEquals("Perto " + t, meus.get(0).nome());
        assertEquals(0.0, meus.get(0).distanciaKm(), 0.001);
        assertEquals("Longe " + t, meus.get(1).nome());
        assertTrue(meus.get(1).distanciaKm() > 80 && meus.get(1).distanciaKm() < 95);
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