package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.SolicitacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.model.Doacao;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusDoacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.model.StatusVoluntariado;
import com.AcolheSP.demo.model.Voluntariado;
import com.AcolheSP.demo.model.Voluntario;

@SpringBootTest
@Transactional
class SolicitacaoServiceTest {

    @Autowired
    private EntityManager em;

    private static String token() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID() + "@exemplo.com";
    }

    private Voluntario criarVoluntario() {
        Voluntario v = new Voluntario();
        v.setEmail(emailUnico("voluntario"));
        v.setRole("usuario");
        v.setNome("Ana Teste");
        v.setTelefone("(11) 11111-1111");
        v.setDisponibilidade("Sábados");
        em.persist(v);
        em.flush();
        return v;
    }

    // Abrigo sem localização (para testes que não precisam de mapa)
    private Instituicao criarInstituicao(String nome) {
        Instituicao i = new Instituicao();
        i.setEmail(emailUnico("abrigo"));
        i.setRole("abrigo");
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

    private Doacao criarDoacao(Voluntario v, Necessidade n, int quantidade, StatusDoacao status, LocalDate data) {
        Doacao d = new Doacao();
        d.setVoluntario(v);
        d.setNecessidade(n);
        d.setQuantidade(quantidade);
        d.setStatus(status);
        d.setData(data);
        em.persist(d);
        em.flush();
        return d;
    }

    // dataSolicitacao null = a entidade preenche com a data de hoje
    private Voluntariado criarVoluntariado(Voluntario v, Instituicao inst, StatusVoluntariado status,
            LocalDate dataSolicitacao) {
        Voluntariado vt = new Voluntariado();
        vt.setVoluntario(v);
        vt.setInstituicao(inst);
        vt.setStatus(status);
        vt.setDataSolicitacao(dataSolicitacao);
        em.persist(vt);
        em.flush();
        return vt;
    }

    private void sincronizar() {
        em.flush();
        em.clear();
    }

    @Autowired
    private SolicitacaoService service;

    private Voluntario voluntario;
    private Instituicao inst;

    @BeforeEach
    void criarDados() {
        voluntario = criarVoluntario();
        inst = criarInstituicao("Casa " + token());
    }

    private SolicitacaoResponse atualizar(Integer id, String tipo, String status) {
        return service.atualizar(inst.getId(), id, new SolicitacaoRequest(tipo, status));
    }

    private ResponseStatusException erro(Integer id, String tipo, String status) {
        return assertThrows(ResponseStatusException.class, () -> atualizar(id, tipo, status));
    }

    private Doacao doacao(StatusDoacao status, int quantidade, Necessidade n) {
        return criarDoacao(voluntario, n, quantidade, status, LocalDate.now());
    }

    // ---------- listar ----------

    @Test
    void naoDeveListarSolicitacoesDeAbrigoInexistente() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listar(Integer.MAX_VALUE));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deveListarVazioQuandoNaoHaSolicitacoes() {
        assertTrue(service.listar(inst.getId()).isEmpty());
    }

    @Test
    void deveListarDoacoesEVoluntariadosDoMaisRecentePraMaisAntigo() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 10, 0, StatusNecessidade.ABERTA);
        Doacao antiga = criarDoacao(voluntario, n, 2, StatusDoacao.PENDENTE, LocalDate.of(2026, 10, 1));
        Doacao nova = criarDoacao(voluntario, n, 2, StatusDoacao.PENDENTE, LocalDate.of(2026, 10, 5));
        Voluntariado vol = criarVoluntariado(voluntario, inst, StatusVoluntariado.PENDENTE, LocalDate.of(2026, 10, 5));
        sincronizar();

        List<SolicitacaoResponse> res = service.listar(inst.getId());

        // mesma data: desempata pelo id maior primeiro
        List<Integer> doDia5 = Stream.of(nova.getId(), vol.getId()).sorted(Comparator.reverseOrder()).toList();
        assertEquals(List.of(doDia5.get(0), doDia5.get(1), antiga.getId()),
                res.stream().map(SolicitacaoResponse::id).toList());
    }

    @Test
    void naoDeveMostrarSolicitacoesDeOutroAbrigo() {
        Instituicao outro = criarInstituicao("Casa " + token());
        Necessidade doOutro = criarNecessidade(outro, "Cobertores", 10, 0, StatusNecessidade.ABERTA);
        doacao(StatusDoacao.PENDENTE, 2, doOutro);
        criarVoluntariado(voluntario, outro, StatusVoluntariado.PENDENTE, null);
        sincronizar();

        assertTrue(service.listar(inst.getId()).isEmpty());
    }

    // ---------- atualizar: validações ----------

    @Test
    void naoDeveAtualizarEmAbrigoInexistente() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(Integer.MAX_VALUE, 1, new SolicitacaoRequest("DOACAO", "APROVADA")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarSemTipoOuSemStatusOuComTipoInvalido() {
        ResponseStatusException semTipo = erro(1, null, "APROVADA");
        ResponseStatusException semStatus = erro(1, "DOACAO", "  ");
        ResponseStatusException tipoInvalido = erro(1, "OUTRO", "APROVADA");

        assertEquals("Campo obrigatório: tipo (DOACAO ou VOLUNTARIADO)", semTipo.getReason());
        assertEquals("Campo obrigatório: status", semStatus.getReason());
        assertEquals("tipo inválido (use DOACAO ou VOLUNTARIADO)", tipoInvalido.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, semTipo.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, semStatus.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, tipoInvalido.getStatusCode());
    }

    // ---------- atualizar: doação ----------

    @Test
    void deveAprovarDoacaoComoConfirmadaENoBanco() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 10, 0, StatusNecessidade.ABERTA);
        Doacao d = doacao(StatusDoacao.PENDENTE, 5, n);

        SolicitacaoResponse res = atualizar(d.getId(), "DOACAO", "APROVADA");

        assertEquals("CONFIRMADA", res.status());
        sincronizar();
        assertEquals(StatusDoacao.CONFIRMADA, em.find(Doacao.class, d.getId()).getStatus());
    }

    @Test
    void deveNormalizarTipoComAcentoEEspacosERecusarComoCancelada() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 10, 0, StatusNecessidade.ABERTA);
        Doacao d = doacao(StatusDoacao.PENDENTE, 5, n);

        SolicitacaoResponse res = atualizar(d.getId(), " Doação ", "recusada");

        assertEquals("CANCELADA", res.status());
    }

    @Test
    void deveEntregarDoacaoSomarNaNecessidadeEFecharQuandoBaterAMeta() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 10, 6, StatusNecessidade.ABERTA);
        Doacao d = doacao(StatusDoacao.CONFIRMADA, 4, n);

        SolicitacaoResponse res = atualizar(d.getId(), "DOACAO", "ENTREGUE");

        assertEquals("ENTREGUE", res.status());
        assertEquals(10, n.getQuantidadeRecebida());
        assertEquals(StatusNecessidade.ATENDIDA, n.getStatus());
    }

    @Test
    void deveEntregarDoacaoSemFecharNecessidadeQuandoNaoBateAMeta() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 10, 6, StatusNecessidade.ABERTA);
        Doacao d = doacao(StatusDoacao.CONFIRMADA, 2, n);

        atualizar(d.getId(), "DOACAO", "ENTREGUE");

        assertEquals(8, n.getQuantidadeRecebida());
        assertEquals(StatusNecessidade.ABERTA, n.getStatus());
    }

    @Test
    void naoDeveAlterarDoacaoJaEntregue() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 10, 10, StatusNecessidade.ATENDIDA);
        Doacao d = doacao(StatusDoacao.ENTREGUE, 4, n);

        ResponseStatusException ex = erro(d.getId(), "DOACAO", "APROVADA");

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarDoacaoComStatusInvalido() {
        Necessidade n = criarNecessidade(inst, "Cobertores", 10, 0, StatusNecessidade.ABERTA);
        Doacao d = doacao(StatusDoacao.PENDENTE, 4, n);

        ResponseStatusException ex = erro(d.getId(), "DOACAO", "EM_ANDAMENTO");

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarDoacaoInexistenteNoAbrigo() {
        ResponseStatusException ex = erro(Integer.MAX_VALUE, "DOACAO", "APROVADA");

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Doação não encontrada nesse abrigo", ex.getReason());
    }

    @Test
    void naoDeveAtualizarDoacaoDeOutroAbrigo() {
        Instituicao outro = criarInstituicao("Casa " + token());
        Necessidade doOutro = criarNecessidade(outro, "Cobertores", 10, 0, StatusNecessidade.ABERTA);
        Doacao d = doacao(StatusDoacao.PENDENTE, 4, doOutro);

        ResponseStatusException ex = erro(d.getId(), "DOACAO", "APROVADA");

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ---------- atualizar: voluntariado ----------

    private Voluntariado voluntariadoCom(StatusVoluntariado status) {
        return criarVoluntariado(voluntario, inst, status, null);
    }

    @Test
    void deveAprovarVoluntariado() {
        Voluntariado v = voluntariadoCom(StatusVoluntariado.PENDENTE);

        assertEquals("APROVADO", atualizar(v.getId(), "VOLUNTARIADO", "aprovada").status());
    }

    @Test
    void deveRecusarVoluntariado() {
        Voluntariado v = voluntariadoCom(StatusVoluntariado.PENDENTE);

        assertEquals("RECUSADO", atualizar(v.getId(), "VOLUNTARIADO", "RECUSADO").status());
    }

    @Test
    void deveColocarVoluntariadoEmAndamentoAceitandoEspacos() {
        Voluntariado v = voluntariadoCom(StatusVoluntariado.APROVADO);

        assertEquals("EM_ANDAMENTO", atualizar(v.getId(), "VOLUNTARIADO", "em andamento").status());
    }

    @Test
    void deveFinalizarVoluntariadoComSinonimos() {
        Voluntariado v = voluntariadoCom(StatusVoluntariado.EM_ANDAMENTO);

        assertEquals("FINALIZADO", atualizar(v.getId(), "VOLUNTARIADO", "concluída").status());
    }

    @Test
    void naoDeveAlterarVoluntariadoEmStatusFinal() {
        Voluntariado v = voluntariadoCom(StatusVoluntariado.RECUSADO);

        ResponseStatusException ex = erro(v.getId(), "VOLUNTARIADO", "APROVADO");

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarVoluntariadoComStatusInvalido() {
        Voluntariado v = voluntariadoCom(StatusVoluntariado.PENDENTE);

        ResponseStatusException ex = erro(v.getId(), "VOLUNTARIADO", "ENTREGUE");

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarVoluntariadoInexistenteNoAbrigo() {
        ResponseStatusException ex = erro(Integer.MAX_VALUE, "VOLUNTARIADO", "APROVADO");

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Voluntariado não encontrado nesse abrigo", ex.getReason());
    }
}