package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import com.AcolheSP.demo.dto.DoacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoRequest;
import com.AcolheSP.demo.dto.VoluntariadoRequest;
import com.AcolheSP.demo.model.*;

/** Cada chamada concorrente abre sua propria transacao real. */
@SpringBootTest
class BackendConcurrencyIntegrationTest {
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager manager;
    @Autowired DoacaoService doacoes;
    @Autowired VoluntariadoService voluntariados;
    @Autowired SolicitacaoService solicitacoes;
    private TransactionTemplate tx;
    private Integer voluntarioId, instituicaoId, necessidadeId;

    @BeforeEach
    void preparar() {
        tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> {
            var v = new Voluntario();
            v.setEmail(UUID.randomUUID() + "@exemplo.com");
            v.setNome("Concorrencia"); v.setRole("usuario");
            em.persist(v);
            var i = new Instituicao(); i.setNome("Casa concorrencia"); em.persist(i);
            var n = new Necessidade(); n.setDescricao("Agua");
            n.setQuantidadeSolicitada(10); n.setInstituicao(i); em.persist(n);
            em.flush();
            voluntarioId = v.getId(); instituicaoId = i.getId(); necessidadeId = n.getId();
        });
    }

    @AfterEach
    void limpar() {
        tx.executeWithoutResult(status -> {
            em.createQuery("delete from Doacao where necessidade.id = :id").setParameter("id", necessidadeId).executeUpdate();
            em.createQuery("delete from Voluntariado where instituicao.id = :id").setParameter("id", instituicaoId).executeUpdate();
            em.remove(em.find(Necessidade.class, necessidadeId));
            em.remove(em.find(Instituicao.class, instituicaoId));
            em.remove(em.find(Voluntario.class, voluntarioId));
        });
    }

    private List<Integer> simultaneas(Runnable primeira, Runnable segunda) throws Exception {
        var barreira = new CyclicBarrier(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Integer> a = () -> executar(barreira, primeira);
            Callable<Integer> b = () -> executar(barreira, segunda);
            var f1 = executor.submit(a);
            var f2 = executor.submit(b);
            return List.of(f1.get(30, TimeUnit.SECONDS), f2.get(30, TimeUnit.SECONDS));
        }
    }

    private int executar(CyclicBarrier barreira, Runnable chamada) throws Exception {
        barreira.await(10, TimeUnit.SECONDS);
        try {
            chamada.run();
            return 200;
        } catch (ResponseStatusException ex) {
            return ex.getStatusCode().value();
        }
    }

    @Test
    void duasPromessasSimultaneasNaoExcedemOSaldo() throws Exception {
        Runnable promessa = () -> doacoes.realizarDoacao(new DoacaoRequest(voluntarioId, necessidadeId, 8));
        var resultados = simultaneas(promessa, promessa);
        assertEquals(1, resultados.stream().filter(s -> s == 200).count());
        assertEquals(1, resultados.stream().filter(s -> s == 409).count());
        tx.executeWithoutResult(status -> assertEquals(8L, em.createQuery(
                "select sum(d.quantidade) from Doacao d where d.necessidade.id = :id", Long.class)
                .setParameter("id", necessidadeId).getSingleResult()));
    }

    @Test
    void duasCandidaturasSimultaneasNaoDuplicamRegistroAtivo() throws Exception {
        Runnable candidatura = () -> voluntariados.demonstrarInteresse(instituicaoId,
                new VoluntariadoRequest(voluntarioId, null, null, null, null));
        var resultados = simultaneas(candidatura, candidatura);
        assertEquals(1, resultados.stream().filter(s -> s == 200).count());
        assertEquals(1, resultados.stream().filter(s -> s == 409).count());
        tx.executeWithoutResult(status -> assertEquals(1L, em.createQuery(
                "select count(v) from Voluntariado v where v.instituicao.id = :id", Long.class)
                .setParameter("id", instituicaoId).getSingleResult()));
    }

    @Test
    void duasEntregasSimultaneasDaMesmaDoacaoContabilizamUmaVez() throws Exception {
        var doacao = doacoes.realizarDoacao(new DoacaoRequest(voluntarioId, necessidadeId, 8));
        solicitacoes.atualizar(instituicaoId, doacao.id(), new SolicitacaoRequest("DOACAO", "CONFIRMADA"));
        Runnable entrega = () -> solicitacoes.atualizar(instituicaoId, doacao.id(),
                new SolicitacaoRequest("DOACAO", "ENTREGUE"));
        var resultados = simultaneas(entrega, entrega);
        assertEquals(1, resultados.stream().filter(s -> s == 200).count());
        assertEquals(1, resultados.stream().filter(s -> s == 409).count());
        tx.executeWithoutResult(status -> assertEquals(8, em.find(Necessidade.class, necessidadeId).getQuantidadeRecebida()));
    }
    @Test
    void entregasDeDoacoesDistintasNaoPerdemAtualizacoesNaNecessidade() throws Exception {
        var d1 = doacoes.realizarDoacao(new DoacaoRequest(voluntarioId, necessidadeId, 4));
        var d2 = doacoes.realizarDoacao(new DoacaoRequest(voluntarioId, necessidadeId, 6));
        for (var d : List.of(d1, d2)) {
            solicitacoes.atualizar(instituicaoId, d.id(), new SolicitacaoRequest("DOACAO", "CONFIRMADA"));
        }
        var resultados = simultaneas(
                () -> solicitacoes.atualizar(instituicaoId, d1.id(), new SolicitacaoRequest("DOACAO", "ENTREGUE")),
                () -> solicitacoes.atualizar(instituicaoId, d2.id(), new SolicitacaoRequest("DOACAO", "ENTREGUE")));
        assertEquals(List.of(200, 200), resultados);
        tx.executeWithoutResult(status -> {
            var n = em.find(Necessidade.class, necessidadeId);
            assertEquals(10, n.getQuantidadeRecebida());
            assertEquals(StatusNecessidade.ATENDIDA, n.getStatus());
        });
    }

}
