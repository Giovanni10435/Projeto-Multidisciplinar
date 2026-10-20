package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Localizacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.NecessidadeRepository;

@ExtendWith(MockitoExtension.class)
class AbrigoServiceUnitTest {

    @Mock
    private InstituicaoRepository instituicaoRepo;

    @Mock
    private ContaRepository contaRepo;

    @Mock
    private NecessidadeRepository necessidadeRepo;

    private final PasswordHasher hasher = new PasswordHasher();
    private AbrigoService service;

    @BeforeEach
    void setUp() {
        service = new AbrigoService(instituicaoRepo, contaRepo, necessidadeRepo, hasher);
    }

    private AbrigoRequest cadastro(String email, String emailContato, String senha) {
        return new AbrigoRequest(email, "Casa Esperança", null, "Acolhimento", "(11) 3333-4444",
                emailContato, "Rua A, 10", "01234-567",
                BigDecimal.valueOf(-23.55), BigDecimal.valueOf(-46.63), senha);
    }

    private AbrigoRequest atualizacao(String nome, String cnpj) {
        return new AbrigoRequest(null, nome, cnpj, null, null, null, null, null, null, null);
    }

    private Instituicao abrigoExistente() {
        Instituicao i = new Instituicao();
        i.setId(1);
        i.setNome("Antigo");
        i.setCnpj("11.111.111/0001-11");
        Localizacao loc = new Localizacao();
        loc.setEndereco("Rua Velha");
        loc.setCep("00000-000");
        loc.setLatitude(BigDecimal.valueOf(-23.0));
        loc.setLongitude(BigDecimal.valueOf(-46.0));
        i.setLocalizacao(loc);
        return i;
    }

    private void salvarDevolvendoComId() {
        when(instituicaoRepo.save(any(Instituicao.class))).thenAnswer(inv -> {
            Instituicao i = inv.getArgument(0);
            i.setId(1);
            return i;
        });
    }

    // ---------- cadastrar ----------

    @Test
    void deveCadastrarAbrigoComSenhaERoleAbrigo() {
        when(contaRepo.existsByEmail("abrigo@exemplo.com")).thenReturn(false);
        salvarDevolvendoComId();

        AbrigoResponse res = service.cadastrarAbrigo(cadastro("abrigo@exemplo.com", "contato@exemplo.com", "senha123"));

        assertEquals("PENDENTE", res.statusVerificacao());

        ArgumentCaptor<Instituicao> captor = ArgumentCaptor.forClass(Instituicao.class);
        verify(instituicaoRepo).save(captor.capture());
        Instituicao salva = captor.getValue();

        assertEquals("abrigo", salva.getRole());
        assertEquals("abrigo@exemplo.com", salva.getEmail());
        assertTrue(salva.isAtivo());
        assertTrue(hasher.confere("senha123", salva.getSenhaHash()));
    }

    @Test
    void deveUsarEmailDeContatoComoLoginQuandoEmailNaoVem() {
        when(contaRepo.existsByEmail("contato@exemplo.com")).thenReturn(false);
        salvarDevolvendoComId();

        service.cadastrarAbrigo(cadastro(null, "contato@exemplo.com", null));

        ArgumentCaptor<Instituicao> captor = ArgumentCaptor.forClass(Instituicao.class);
        verify(instituicaoRepo).save(captor.capture());
        assertEquals("contato@exemplo.com", captor.getValue().getEmail());
    }

    @Test
    void naoDeveCadastrarAbrigoSemEnderecoNemEmail() {
        ResponseStatusException semEmail = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarAbrigo(cadastro(null, null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, semEmail.getStatusCode());

        AbrigoRequest semEndereco = new AbrigoRequest("a@a.com", "Casa", null, null, null, null, null, null,
                BigDecimal.ONE, BigDecimal.ONE);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarAbrigo(semEndereco));
        assertEquals("Campo obrigatório: endereco", ex.getReason());
        verify(instituicaoRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarAbrigoComSenhaCurta() {
        when(contaRepo.existsByEmail("abrigo@exemplo.com")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarAbrigo(cadastro("abrigo@exemplo.com", null, "123")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(instituicaoRepo, never()).save(any());
    }

    // ---------- listar ----------

    @Test
    void deveListarPorNomeQuandoNaoHaFiltroDeNecessidade() {
        Instituicao i = abrigoExistente();
        when(instituicaoRepo.findByNomeContainingIgnoreCaseOrderByNome("casa")).thenReturn(List.of(i));

        List<AbrigoResponse> res = service.listarAbrigos("  casa  ", null);

        assertEquals(1, res.size());
        assertEquals("Antigo", res.get(0).nome());
        verify(instituicaoRepo, never()).buscarPorNomeENecessidade(anyString(), anyString(), any());
    }

    @Test
    void deveListarTodosQuandoNomeENecessidadeSaoNulos() {
        when(instituicaoRepo.findByNomeContainingIgnoreCaseOrderByNome("")).thenReturn(List.of());

        assertTrue(service.listarAbrigos(null, null).isEmpty());
    }

    @Test
    void deveBuscarPorNomeENecessidadeAberta() {
        when(instituicaoRepo.buscarPorNomeENecessidade("casa", "cobertor", StatusNecessidade.ABERTA))
                .thenReturn(List.of(abrigoExistente()));

        List<AbrigoResponse> res = service.listarAbrigos("casa", " cobertor ");

        assertEquals(1, res.size());
        verify(instituicaoRepo, never()).findByNomeContainingIgnoreCaseOrderByNome(anyString());
    }

    // ---------- atualizar ----------

    @Test
    void naoDeveAtualizarAbrigoInexistente() {
        when(instituicaoRepo.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarAbrigo(1, atualizacao("Novo", null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarParaCnpjJaUsadoPorOutroAbrigo() {
        when(instituicaoRepo.findById(1)).thenReturn(Optional.of(abrigoExistente()));
        when(instituicaoRepo.existsByCnpj("22.222.222/0001-22")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarAbrigo(1, atualizacao(null, "22.222.222/0001-22")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(instituicaoRepo, never()).save(any());
    }

    @Test
    void naoDeveConsultarDuplicidadeQuandoCnpjNaoMudou() {
        Instituicao i = abrigoExistente();
        when(instituicaoRepo.findById(1)).thenReturn(Optional.of(i));
        when(instituicaoRepo.save(i)).thenReturn(i);

        service.atualizarAbrigo(1, atualizacao(null, "11.111.111/0001-11"));

        verify(instituicaoRepo, never()).existsByCnpj(anyString());
    }

    @Test
    void deveAtualizarLocalizacaoEDemaisCamposInformados() {
        Instituicao i = abrigoExistente();
        when(instituicaoRepo.findById(1)).thenReturn(Optional.of(i));
        when(instituicaoRepo.existsByCnpj("22.222.222/0001-22")).thenReturn(false);
        when(instituicaoRepo.save(i)).thenReturn(i);

        AbrigoRequest req = new AbrigoRequest(null, "Novo", "22.222.222/0001-22", "Nova descrição", "(11) 9", "novo@x.com",
                "Rua Nova, 1", "11111-111", BigDecimal.valueOf(-22.0), BigDecimal.valueOf(-47.0));

        AbrigoResponse res = service.atualizarAbrigo(1, req);

        assertEquals("Novo", res.nome());
        assertEquals("22.222.222/0001-22", res.cnpj());
        assertEquals("Nova descrição", res.descricao());
        assertEquals("novo@x.com", res.emailContato());
        assertEquals("Rua Nova, 1", res.endereco());
        assertEquals("11111-111", res.cep());
        assertEquals(0, BigDecimal.valueOf(-22.0).compareTo(res.latitude()));
        assertEquals(0, BigDecimal.valueOf(-47.0).compareTo(res.longitude()));
    }
}