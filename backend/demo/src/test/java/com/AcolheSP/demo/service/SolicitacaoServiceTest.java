package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.SolicitacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.model.Doacao;
import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusDoacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.model.StatusVoluntariado;
import com.AcolheSP.demo.model.Voluntariado;
import com.AcolheSP.demo.model.Voluntario;
import com.AcolheSP.demo.repository.DoacaoRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.VoluntariadoRepository;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTest {

    private static final Integer INST_ID = 1;

    @Mock
    private DoacaoRepository doacaoRepo;

    @Mock
    private VoluntariadoRepository voluntariadoRepo;

    @Mock
    private InstituicaoRepository instituicaoRepo;

    private SolicitacaoService service;
    private Voluntario voluntario;

    @BeforeEach
    void setUp() {
        service = new SolicitacaoService(doacaoRepo, voluntariadoRepo, instituicaoRepo);
        voluntario = new Voluntario();
        voluntario.setId(5);
        voluntario.setNome("Ana");
    }

    // ---------- helpers ----------

    private void instituicaoExiste() {
        when(instituicaoRepo.existsById(INST_ID)).thenReturn(true);
    }

    private Necessidade necessidade(int solicitada, int recebida) {
        Necessidade n = new Necessidade();
        n.setId(7);
        n.setDescricao("Cobertores");
        n.setQuantidadeSolicitada(solicitada);
        n.setQuantidadeRecebida(recebida);
        n.setStatus(StatusNecessidade.ABERTA);
        return n;
    }

    private Doacao doacao(int id, StatusDoacao status, int quantidade, Necessidade n, LocalDate data) {
        Doacao d = new Doacao();
        d.setId(id);
        d.setStatus(status);
        d.setQuantidade(quantidade);
        d.setNecessidade(n);
        d.setVoluntario(voluntario);
        d.setData(data);
        return d;
    }

    private Voluntariado voluntariado(int id, StatusVoluntariado status, LocalDate data) {
        Voluntariado v = new Voluntariado();
        v.setId(id);
        v.setStatus(status);
        v.setVoluntario(voluntario);
        v.setDataSolicitacao(data);
        return v;
    }

    private void doacaoExiste(Doacao d) {
        when(doacaoRepo.findByIdAndNecessidadeInstituicaoId(d.getId(), INST_ID)).thenReturn(Optional.of(d));
    }

    private void voluntariadoExiste(Voluntariado v) {
        when(voluntariadoRepo.findByIdAndInstituicaoId(v.getId(), INST_ID)).thenReturn(Optional.of(v));
    }

    private ResponseStatusException erro(Integer id, String tipo, String status) {
        return assertThrows(ResponseStatusException.class,
                () -> service.atualizar(INST_ID, id, new SolicitacaoRequest(tipo, status)));
    }

    // ---------- listar ----------

    @Test
    void naoDeveListarSolicitacoesDeAbrigoInexistente() {
        when(instituicaoRepo.existsById(INST_ID)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.listar(INST_ID));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deveListarDoacoesEVoluntariadosDoMaisRecentePraMaisAntigo() {
        instituicaoExiste();
        Necessidade n = necessidade(10, 0);
        when(doacaoRepo.findByNecessidadeInstituicaoId(INST_ID)).thenReturn(List.of(
                doacao(1, StatusDoacao.PENDENTE, 2, n, LocalDate.of(2026, 10, 1)),
                doacao(3, StatusDoacao.PENDENTE, 2, n, LocalDate.of(2026, 10, 5))));
        when(voluntariadoRepo.findByInstituicaoId(INST_ID)).thenReturn(List.of(
                voluntariado(2, StatusVoluntariado.PENDENTE, LocalDate.of(2026, 10, 5))));

        List<SolicitacaoResponse> res = service.listar(INST_ID);

        // mesma data: desempata pelo id maior primeiro
        assertEquals(List.of(3, 2, 1), res.stream().map(SolicitacaoResponse::id).toList());
        assertEquals("DOACAO", res.get(0).tipo());
        assertEquals("VOLUNTARIADO", res.get(1).tipo());
    }

    @Test
    void deveListarVazioQuandoNaoHaSolicitacoes() {
        instituicaoExiste();
        when(doacaoRepo.findByNecessidadeInstituicaoId(INST_ID)).thenReturn(List.of());
        when(voluntariadoRepo.findByInstituicaoId(INST_ID)).thenReturn(List.of());

        assertEquals(0, service.listar(INST_ID).size());
    }

    // ---------- atualizar: validações ----------

    @Test
    void naoDeveAtualizarEmAbrigoInexistente() {
        when(instituicaoRepo.existsById(INST_ID)).thenReturn(false);

        assertEquals(HttpStatus.NOT_FOUND, erro(1, "DOACAO", "APROVADA").getStatusCode());
    }

    @Test
    void naoDeveAtualizarSemTipo() {
        instituicaoExiste();

        ResponseStatusException ex = erro(1, null, "APROVADA");

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: tipo (DOACAO ou VOLUNTARIADO)", ex.getReason());
    }

    @Test
    void naoDeveAtualizarSemStatus() {
        instituicaoExiste();

        ResponseStatusException ex = erro(1, "DOACAO", "  ");

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: status", ex.getReason());
    }

    @Test
    void naoDeveAtualizarComTipoInvalido() {
        instituicaoExiste();

        ResponseStatusException ex = erro(1, "OUTRO", "APROVADA");

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("tipo inválido (use DOACAO ou VOLUNTARIADO)", ex.getReason());
    }

    // ---------- atualizar: doação ----------

    @Test
    void deveAprovarDoacaoComoConfirmada() {
        instituicaoExiste();
        Doacao d = doacao(1, StatusDoacao.PENDENTE, 5, necessidade(10, 0), LocalDate.now());
        doacaoExiste(d);
        when(doacaoRepo.save(d)).thenReturn(d);

        SolicitacaoResponse res = service.atualizar(INST_ID, 1, new SolicitacaoRequest("DOACAO", "APROVADA"));

        assertEquals("CONFIRMADA", res.status());
    }

    @Test
    void deveNormalizarTipoComAcentoEEspacos() {
        instituicaoExiste();
        Doacao d = doacao(1, StatusDoacao.PENDENTE, 5, necessidade(10, 0), LocalDate.now());
        doacaoExiste(d);
        when(doacaoRepo.save(d)).thenReturn(d);

        SolicitacaoResponse res = service.atualizar(INST_ID, 1, new SolicitacaoRequest(" Doação ", "recusada"));

        assertEquals("CANCELADA", res.status());
    }

    @Test
    void deveEntregarDoacaoSomarNaNecessidadeEFecharQuandoBaterAMeta() {
        instituicaoExiste();
        Necessidade n = necessidade(10, 6);
        Doacao d = doacao(1, StatusDoacao.CONFIRMADA, 4, n, LocalDate.now());
        doacaoExiste(d);
        when(doacaoRepo.save(d)).thenReturn(d);

        SolicitacaoResponse res = service.atualizar(INST_ID, 1, new SolicitacaoRequest("DOACAO", "ENTREGUE"));

        assertEquals("ENTREGUE", res.status());
        assertEquals(10, n.getQuantidadeRecebida());
        assertEquals(StatusNecessidade.ATENDIDA, n.getStatus());
    }

    @Test
    void deveEntregarDoacaoSemFecharNecessidadeQuandoNaoBateAMeta() {
        instituicaoExiste();
        Necessidade n = necessidade(10, 6);
        Doacao d = doacao(1, StatusDoacao.CONFIRMADA, 2, n, LocalDate.now());
        doacaoExiste(d);
        when(doacaoRepo.save(d)).thenReturn(d);

        service.atualizar(INST_ID, 1, new SolicitacaoRequest("DOACAO", "ENTREGUE"));

        assertEquals(8, n.getQuantidadeRecebida());
        assertEquals(StatusNecessidade.ABERTA, n.getStatus());
    }

    @Test
    void naoDeveAlterarDoacaoJaEntregueOuCancelada() {
        instituicaoExiste();
        Doacao d = doacao(1, StatusDoacao.ENTREGUE, 4, necessidade(10, 10), LocalDate.now());
        doacaoExiste(d);

        ResponseStatusException ex = erro(1, "DOACAO", "APROVADA");

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(doacaoRepo, never()).save(any());
    }

    @Test
    void naoDeveAtualizarDoacaoComStatusInvalido() {
        instituicaoExiste();
        doacaoExiste(doacao(1, StatusDoacao.PENDENTE, 4, necessidade(10, 0), LocalDate.now()));

        ResponseStatusException ex = erro(1, "DOACAO", "EM_ANDAMENTO");

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarDoacaoInexistenteNoAbrigo() {
        instituicaoExiste();
        when(doacaoRepo.findByIdAndNecessidadeInstituicaoId(99, INST_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = erro(99, "DOACAO", "APROVADA");

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Doação não encontrada nesse abrigo", ex.getReason());
    }

    // ---------- atualizar: voluntariado ----------

    private SolicitacaoResponse atualizarVoluntariado(Voluntariado v, String status) {
        voluntariadoExiste(v);
        when(voluntariadoRepo.save(v)).thenReturn(v);
        return service.atualizar(INST_ID, v.getId(), new SolicitacaoRequest("VOLUNTARIADO", status));
    }

    @Test
    void deveAprovarVoluntariado() {
        instituicaoExiste();
        Voluntariado v = voluntariado(2, StatusVoluntariado.PENDENTE, LocalDate.now());

        assertEquals("APROVADO", atualizarVoluntariado(v, "aprovada").status());
    }

    @Test
    void deveRecusarVoluntariado() {
        instituicaoExiste();
        Voluntariado v = voluntariado(2, StatusVoluntariado.PENDENTE, LocalDate.now());

        assertEquals("RECUSADO", atualizarVoluntariado(v, "RECUSADO").status());
    }

    @Test
    void deveColocarVoluntariadoEmAndamentoAceitandoEspacos() {
        instituicaoExiste();
        Voluntariado v = voluntariado(2, StatusVoluntariado.APROVADO, LocalDate.now());

        assertEquals("EM_ANDAMENTO", atualizarVoluntariado(v, "em andamento").status());
    }

    @Test
    void deveFinalizarVoluntariadoComSinonimos() {
        instituicaoExiste();
        Voluntariado v = voluntariado(2, StatusVoluntariado.EM_ANDAMENTO, LocalDate.now());

        assertEquals("FINALIZADO", atualizarVoluntariado(v, "concluída").status());
    }

    @Test
    void naoDeveAlterarVoluntariadoEmStatusFinal() {
        instituicaoExiste();
        voluntariadoExiste(voluntariado(2, StatusVoluntariado.RECUSADO, LocalDate.now()));

        ResponseStatusException ex = erro(2, "VOLUNTARIADO", "APROVADO");

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(voluntariadoRepo, never()).save(any());
    }

    @Test
    void naoDeveAtualizarVoluntariadoComStatusInvalido() {
        instituicaoExiste();
        voluntariadoExiste(voluntariado(2, StatusVoluntariado.PENDENTE, LocalDate.now()));

        ResponseStatusException ex = erro(2, "VOLUNTARIADO", "ENTREGUE");

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarVoluntariadoInexistenteNoAbrigo() {
        instituicaoExiste();
        when(voluntariadoRepo.findByIdAndInstituicaoId(99, INST_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = erro(99, "VOLUNTARIADO", "APROVADO");

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Voluntariado não encontrado nesse abrigo", ex.getReason());
    }
}