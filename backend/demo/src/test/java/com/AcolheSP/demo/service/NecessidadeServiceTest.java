package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.NecessidadeRequest;
import com.AcolheSP.demo.dto.NecessidadeResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.NecessidadeRepository;

@ExtendWith(MockitoExtension.class)
class NecessidadeServiceTest {

    private static final Integer INST_ID = 1;

    @Mock
    private NecessidadeRepository necessidadeRepo;

    @Mock
    private InstituicaoRepository instituicaoRepo;

    private NecessidadeService service;
    private Instituicao instituicao;

    @BeforeEach
    void setUp() {
        service = new NecessidadeService(necessidadeRepo, instituicaoRepo);
        instituicao = new Instituicao();
        instituicao.setId(INST_ID);
    }

    private void instituicaoExiste() {
        when(instituicaoRepo.findById(INST_ID)).thenReturn(Optional.of(instituicao));
    }

    private void salvarDevolvendoComId() {
        when(necessidadeRepo.save(any(Necessidade.class))).thenAnswer(inv -> {
            Necessidade n = inv.getArgument(0);
            n.setId(7);
            return n;
        });
    }

    private Necessidade necessidade(int solicitada, int recebida, StatusNecessidade status) {
        Necessidade n = new Necessidade();
        n.setId(7);
        n.setDescricao("Cobertores");
        n.setQuantidadeSolicitada(solicitada);
        n.setQuantidadeRecebida(recebida);
        n.setStatus(status);
        n.setInstituicao(instituicao);
        return n;
    }

    private void necessidadeExiste(Necessidade n) {
        when(necessidadeRepo.findByIdAndInstituicaoId(7, INST_ID)).thenReturn(Optional.of(n));
    }

    private ResponseStatusException erroAoCadastrar(NecessidadeRequest req) {
        return assertThrows(ResponseStatusException.class, () -> service.cadastrarNecessidade(INST_ID, req));
    }

    // ---------- cadastrar ----------

    @Test
    void deveCadastrarNecessidadeComPadroes() {
        instituicaoExiste();
        salvarDevolvendoComId();

        NecessidadeResponse res = service.cadastrarNecessidade(INST_ID,
                new NecessidadeRequest("Cobertores", 50, null, null));

        assertEquals(7, res.id());
        assertEquals("Cobertores", res.descricao());
        assertEquals(50, res.quantidadeSolicitada());
        assertEquals(0, res.quantidadeRecebida());
        assertEquals("ABERTA", res.status());
        assertEquals(INST_ID, res.instituicaoId());
    }

    @Test
    void deveCadastrarNecessidadeComStatusEQuantidadeRecebidaInformados() {
        instituicaoExiste();
        salvarDevolvendoComId();

        NecessidadeResponse res = service.cadastrarNecessidade(INST_ID,
                new NecessidadeRequest("Cobertores", 50, 10, "atendida"));

        assertEquals(10, res.quantidadeRecebida());
        assertEquals("ATENDIDA", res.status());
    }

    @Test
    void naoDeveCadastrarEmAbrigoInexistente() {
        when(instituicaoRepo.findById(INST_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest("Cobertores", 50, null, null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(necessidadeRepo, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "   " })
    void naoDeveCadastrarSemDescricao(String descricao) {
        instituicaoExiste();

        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest(descricao, 50, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: descricao", ex.getReason());
        verify(necessidadeRepo, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 0, -1, -50 })
    void naoDeveCadastrarComQuantidadeSolicitadaInvalida(Integer quantidade) {
        instituicaoExiste();

        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest("Cobertores", quantidade, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("quantidadeSolicitada deve ser maior que zero", ex.getReason());
    }

    @Test
    void naoDeveCadastrarComQuantidadeRecebidaNegativa() {
        instituicaoExiste();

        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest("Cobertores", 50, -1, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("quantidadeRecebida não pode ser negativa", ex.getReason());
    }

    @Test
    void naoDeveCadastrarComStatusInvalido() {
        instituicaoExiste();

        ResponseStatusException ex = erroAoCadastrar(new NecessidadeRequest("Cobertores", 50, null, "qualquer"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().startsWith("status inválido"));
    }

    // ---------- atualizar ----------

    @Test
    void deveAtualizarSomenteOsCamposInformados() {
        Necessidade n = necessidade(50, 5, StatusNecessidade.ABERTA);
        necessidadeExiste(n);
        when(necessidadeRepo.save(n)).thenReturn(n);

        NecessidadeResponse res = service.atualizarNecessidade(INST_ID, 7,
                new NecessidadeRequest("Cobertores de lã", null, null, null));

        assertEquals("Cobertores de lã", res.descricao());
        assertEquals(50, res.quantidadeSolicitada());
        assertEquals(5, res.quantidadeRecebida());
        assertEquals("ABERTA", res.status());
    }

    @Test
    void deveAtualizarQuantidadesEStatus() {
        Necessidade n = necessidade(50, 5, StatusNecessidade.ABERTA);
        necessidadeExiste(n);
        when(necessidadeRepo.save(n)).thenReturn(n);

        NecessidadeResponse res = service.atualizarNecessidade(INST_ID, 7,
                new NecessidadeRequest(null, 80, 80, "ATENDIDA"));

        assertEquals(80, res.quantidadeSolicitada());
        assertEquals(80, res.quantidadeRecebida());
        assertEquals("ATENDIDA", res.status());
    }

    @Test
    void naoDeveAtualizarNecessidadeInexistenteNoAbrigo() {
        when(necessidadeRepo.findByIdAndInstituicaoId(7, INST_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarNecessidade(INST_ID, 7, new NecessidadeRequest("x", null, null, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(necessidadeRepo, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -3 })
    void naoDeveAtualizarComQuantidadeSolicitadaInvalida(int quantidade) {
        necessidadeExiste(necessidade(50, 5, StatusNecessidade.ABERTA));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarNecessidade(INST_ID, 7, new NecessidadeRequest(null, quantidade, null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(necessidadeRepo, never()).save(any());
    }

    @Test
    void naoDeveAtualizarComQuantidadeRecebidaNegativa() {
        necessidadeExiste(necessidade(50, 5, StatusNecessidade.ABERTA));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarNecessidade(INST_ID, 7, new NecessidadeRequest(null, null, -1, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarComStatusInvalido() {
        necessidadeExiste(necessidade(50, 5, StatusNecessidade.ABERTA));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarNecessidade(INST_ID, 7, new NecessidadeRequest(null, null, null, "xyz")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ---------- remover ----------

    @Test
    void deveRemoverNecessidade() {
        Necessidade n = necessidade(50, 0, StatusNecessidade.ABERTA);
        necessidadeExiste(n);

        service.removerNecessidade(INST_ID, 7);

        verify(necessidadeRepo).delete(n);
    }

    @Test
    void naoDeveRemoverNecessidadeInexistenteNoAbrigo() {
        when(necessidadeRepo.findByIdAndInstituicaoId(7, INST_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.removerNecessidade(INST_ID, 7));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(necessidadeRepo, never()).delete(any(Necessidade.class));
    }

    // ---------- listar ----------

    @Test
    void deveListarNecessidadesDoAbrigo() {
        instituicaoExiste();
        when(necessidadeRepo.findByInstituicaoId(INST_ID))
                .thenReturn(List.of(necessidade(50, 0, StatusNecessidade.ABERTA)));

        List<NecessidadeResponse> lista = service.listarNecessidades(INST_ID);

        assertEquals(1, lista.size());
        assertEquals("Cobertores", lista.get(0).descricao());
    }

    @Test
    void naoDeveListarNecessidadesDeAbrigoInexistente() {
        when(instituicaoRepo.findById(INST_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.listarNecessidades(INST_ID));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(necessidadeRepo, never()).findByInstituicaoId(any());
    }
}