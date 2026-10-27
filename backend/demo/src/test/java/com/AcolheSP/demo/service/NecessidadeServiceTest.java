package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.NecessidadeRequest;
import com.AcolheSP.demo.dto.NecessidadeResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.NecessidadeRepository;

@SpringBootTest
@Transactional
class NecessidadeServiceTest {

    @Autowired
    private EntityManager em;

    private static String token() {
        return UUID.randomUUID().toString().substring(0, 8);
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

    private static final int ABRIGO_INEXISTENTE = Integer.MAX_VALUE;

    @Autowired
    private NecessidadeService service;

    @Autowired
    private NecessidadeRepository necessidadeRepo;

    private Instituicao inst;

    @BeforeEach
    void criarAbrigo() {
        inst = criarInstituicao("Casa " + token());
    }

    private ResponseStatusException erroAoCadastrar(NecessidadeRequest req) {
        return assertThrows(ResponseStatusException.class, () -> service.cadastrarNecessidade(inst.getId(), req));
    }

    // ---------- cadastrar ----------

    @Test
    void deveCadastrarNecessidadeComPadroesESalvarNoBanco() {
        NecessidadeResponse res = service.cadastrarNecessidade(inst.getId(),
                new NecessidadeRequest("Cobertores", 50, null, null));

        assertEquals("Cobertores", res.descricao());
        assertEquals(50, res.quantidadeSolicitada());
        assertEquals(0, res.quantidadeRecebida());
        assertEquals("ABERTA", res.status());
        assertEquals(inst.getId(), res.instituicaoId());

        sincronizar();
        assertEquals(1, necessidadeRepo.findByInstituicaoId(inst.getId()).size());
    }

    @Test
    void deveCadastrarNecessidadeComStatusEQuantidadeRecebidaInformados() {
        NecessidadeResponse res = service.cadastrarNecessidade(inst.getId(),
                new NecessidadeRequest("Cobertores", 50, 10, "atendida"));

        assertEquals(10, res.quantidadeRecebida());
        assertEquals("ATENDIDA", res.status());
    }

    @Test
    void naoDeveCadastrarEmAbrigoInexistente() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarNecessidade(ABRIGO_INEXISTENTE,
                        new NecessidadeRequest("Cobertores", 50, null, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "   " })
    void naoDeveCadastrarSemDescricao(String descricao) {
        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest(descricao, 50, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: descricao", ex.getReason());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 0, -1, -50 })
    void naoDeveCadastrarComQuantidadeSolicitadaInvalida(Integer quantidade) {
        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest("Cobertores", quantidade, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("quantidadeSolicitada deve ser maior que zero", ex.getReason());
    }

    @Test
    void naoDeveCadastrarComQuantidadeRecebidaNegativa() {
        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest("Cobertores", 50, -1, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("quantidadeRecebida não pode ser negativa", ex.getReason());
    }

    @Test
    void naoDeveCadastrarComStatusInvalido() {
        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest("Cobertores", 50, null, "qualquer"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().startsWith("status inválido"));
    }

    // ---------- atualizar ----------

    @Test
    void deveAtualizarSomenteOsCamposInformados() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 50, 5, StatusNecessidade.ABERTA);

        NecessidadeResponse res = service.atualizarNecessidade(inst.getId(), n.getId(),
                new NecessidadeRequest("Cobertores de lã", null, null, null));

        assertEquals("Cobertores de lã", res.descricao());
        assertEquals(50, res.quantidadeSolicitada());
        assertEquals(5, res.quantidadeRecebida());
        assertEquals("ABERTA", res.status());
    }

    @Test
    void deveAtualizarQuantidadesEStatusENoBanco() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 50, 5, StatusNecessidade.ABERTA);

        NecessidadeResponse res = service.atualizarNecessidade(inst.getId(), n.getId(),
                new NecessidadeRequest(null, 80, 80, "ATENDIDA"));

        assertEquals(80, res.quantidadeSolicitada());
        assertEquals(80, res.quantidadeRecebida());
        assertEquals("ATENDIDA", res.status());

        sincronizar();
        Necessidade doBanco = necessidadeRepo.findById(n.getId()).orElseThrow();
        assertEquals(80, doBanco.getQuantidadeRecebida());
        assertEquals(StatusNecessidade.ATENDIDA, doBanco.getStatus());
    }

    @Test
    void naoDeveAtualizarNecessidadeDeOutroAbrigo() {
        Instituicao outro = criarInstituicao("Casa " + token());
        Necessidade doOutro = criarNecessidade(outro, "Cobertores", 50, 0, StatusNecessidade.ABERTA);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarNecessidade(inst.getId(), doOutro.getId(),
                        new NecessidadeRequest("x", null, null, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -3 })
    void naoDeveAtualizarComQuantidadeSolicitadaInvalida(int quantidade) {
        Necessidade n = criarNecessidade(inst, "Cobertores", 50, 5, StatusNecessidade.ABERTA);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarNecessidade(inst.getId(), n.getId(),
                        new NecessidadeRequest(null, quantidade, null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarComQuantidadeRecebidaNegativaOuStatusInvalido() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 50, 5, StatusNecessidade.ABERTA);

        ResponseStatusException negativa = assertThrows(ResponseStatusException.class,
                () -> service.atualizarNecessidade(inst.getId(), n.getId(),
                        new NecessidadeRequest(null, null, -1, null)));
        ResponseStatusException invalido = assertThrows(ResponseStatusException.class,
                () -> service.atualizarNecessidade(inst.getId(), n.getId(),
                        new NecessidadeRequest(null, null, null, "xyz")));

        assertEquals(HttpStatus.BAD_REQUEST, negativa.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, invalido.getStatusCode());
    }

    // ---------- remover ----------

    @Test
    void deveRemoverNecessidadeDoBanco() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 50, 0, StatusNecessidade.ABERTA);

        service.removerNecessidade(inst.getId(), n.getId());

        sincronizar();
        assertTrue(necessidadeRepo.findById(n.getId()).isEmpty());
    }

    @Test
    void naoDeveRemoverNecessidadeInexistenteNoAbrigo() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.removerNecessidade(inst.getId(), Integer.MAX_VALUE));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ---------- listar ----------

    @Test
    void deveListarSoAsNecessidadesDoAbrigo() {
        Instituicao outro = criarInstituicao("Casa " + token());
        criarNecessidade(inst, "Cobertores", 50, 0, StatusNecessidade.ABERTA);
        criarNecessidade(inst, "Alimentos", 20, 20, StatusNecessidade.ATENDIDA);
        criarNecessidade(outro, "Roupas", 10, 0, StatusNecessidade.ABERTA);
        sincronizar();

        List<NecessidadeResponse> lista = service.listarNecessidades(inst.getId());

        assertEquals(2, lista.size());
    }

    @Test
    void naoDeveListarNecessidadesDeAbrigoInexistente() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listarNecessidades(ABRIGO_INEXISTENTE));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}