package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.ContaRepository;

/**
 * Complementa o AbrigoServiceTest: senha, e-mail de contato como login,
 * listagem com filtros e atualização de campos, tudo no banco.
 */
@SpringBootTest
@Transactional
class AbrigoServiceUnitTest {

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

    private void sincronizar() {
        em.flush();
        em.clear();
    }

    @Autowired
    private AbrigoService service;

    @Autowired
    private ContaRepository contaRepo;

    @Autowired
    private PasswordHasher hasher;

    private static String cnpjAleatorio() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return String.format("%02d.%03d.%03d/0001-%02d", r.nextInt(100), r.nextInt(1000), r.nextInt(1000), r.nextInt(100));
    }

    private AbrigoRequest cadastro(String email, String nome, String emailContato, String senha) {
        return new AbrigoRequest(email, nome, null, "Acolhimento", "(11) 3333-4444", emailContato,
                "Rua A, 10", "01234-567", new BigDecimal("-23.55"), new BigDecimal("-46.63"), senha);
    }

    private AbrigoResponse criar(String nome) {
        return service.cadastrarAbrigo(cadastro(emailUnico("abrigo"), nome, null, null));
    }

    // ---------- cadastrar ----------

    @Test
    void deveCadastrarAbrigoComSenhaERoleAbrigo() {
        String email = emailUnico("abrigo");

        AbrigoResponse res = service.cadastrarAbrigo(cadastro(email, "Casa " + token(), null, "senha123"));

        assertEquals("PENDENTE", res.statusVerificacao());
        sincronizar();
        Conta conta = contaRepo.findByEmail(email).orElseThrow();
        assertEquals("abrigo", conta.getRole());
        assertTrue(conta.isAtivo());
        assertNotEquals("senha123", conta.getSenhaHash());
        assertTrue(hasher.confere("senha123", conta.getSenhaHash()));
    }

    @Test
    void deveUsarEmailDeContatoComoLoginQuandoEmailNaoVem() {
        String emailContato = emailUnico("contato");

        service.cadastrarAbrigo(cadastro(null, "Casa " + token(), emailContato, null));

        assertTrue(contaRepo.existsByEmail(emailContato));
    }

    @Test
    void naoDeveCadastrarSemEmailNemEmailDeContato() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarAbrigo(cadastro(null, "Casa " + token(), null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveCadastrarSemEndereco() {
        AbrigoRequest semEndereco = new AbrigoRequest(emailUnico("abrigo"), "Casa " + token(), null, null, null,
                null, null, null, BigDecimal.ONE, BigDecimal.ONE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarAbrigo(semEndereco));

        assertEquals("Campo obrigatório: endereco", ex.getReason());
    }

    @Test
    void naoDeveCadastrarComSenhaCurta() {
        String email = emailUnico("abrigo");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarAbrigo(cadastro(email, "Casa " + token(), null, "123")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertFalse(contaRepo.existsByEmail(email));
    }

    // ---------- listar ----------

    @Test
    void deveListarPorNomeIgnorandoMaiusculasEOrdenandoPorNome() {
        String t = token();
        criar("Gama " + t);
        criar("Alfa " + t);
        criar("Beta " + t);
        sincronizar();

        List<AbrigoResponse> res = service.listarAbrigos("  " + t.toUpperCase() + "  ", null);

        assertEquals(List.of("Alfa " + t, "Beta " + t, "Gama " + t), res.stream().map(AbrigoResponse::nome).toList());
    }

    @Test
    void deveFiltrarPorNecessidadeAbertaIgnorandoMaiusculas() {
        String t = token();
        AbrigoResponse comAberta = criar("Casa " + t + " A");
        AbrigoResponse comAtendida = criar("Casa " + t + " B");
        AbrigoResponse outraNecessidade = criar("Casa " + t + " C");
        criarNecessidade(em.find(Instituicao.class, comAberta.id()), "Cobertores de lã", 10, 0, StatusNecessidade.ABERTA);
        criarNecessidade(em.find(Instituicao.class, comAtendida.id()), "Cobertores", 10, 10, StatusNecessidade.ATENDIDA);
        criarNecessidade(em.find(Instituicao.class, outraNecessidade.id()), "Alimentos", 10, 0, StatusNecessidade.ABERTA);
        sincronizar();

        List<AbrigoResponse> res = service.listarAbrigos(t, " COBERTOR ");

        assertEquals(List.of(comAberta.id()), res.stream().map(AbrigoResponse::id).toList());
    }

    @Test
    void listarSemFiltrosNaoDeveDarErro() {
        criar("Casa " + token());
        sincronizar();

        assertFalse(service.listarAbrigos(null, null).isEmpty());
    }

    // ---------- atualizar ----------

    @Test
    void naoDeveAtualizarAbrigoInexistente() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarAbrigo(Integer.MAX_VALUE,
                        new AbrigoRequest(null, "Novo", null, null, null, null, null, null, null, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void naoDeveAtualizarParaCnpjJaUsadoPorOutroAbrigo() {
        String cnpj = cnpjAleatorio();
        service.atualizarAbrigo(criar("Casa " + token()).id(),
                new AbrigoRequest(null, null, cnpj, null, null, null, null, null, null, null));
        AbrigoResponse outro = criar("Casa " + token());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.atualizarAbrigo(outro.id(),
                        new AbrigoRequest(null, null, cnpj, null, null, null, null, null, null, null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void devePermitirReenviarOMesmoCnpjDoProprioAbrigo() {
        String cnpj = cnpjAleatorio();
        AbrigoResponse abrigo = criar("Casa " + token());
        AbrigoRequest mudaCnpj = new AbrigoRequest(null, null, cnpj, null, null, null, null, null, null, null);
        service.atualizarAbrigo(abrigo.id(), mudaCnpj);

        AbrigoResponse res = service.atualizarAbrigo(abrigo.id(), mudaCnpj);

        assertEquals(cnpj, res.cnpj());
    }

    @Test
    void deveAtualizarLocalizacaoEDemaisCamposInformados() {
        AbrigoResponse abrigo = criar("Casa " + token());
        AbrigoRequest req = new AbrigoRequest(null, "Nome novo", null, "Nova descrição", "(11) 90000-0000",
                "novo@exemplo.com", "Rua Nova, 1", "11111-111", new BigDecimal("-22.000000"),
                new BigDecimal("-47.000000"));

        AbrigoResponse res = service.atualizarAbrigo(abrigo.id(), req);

        assertEquals("Nome novo", res.nome());
        assertEquals("Nova descrição", res.descricao());
        assertEquals("novo@exemplo.com", res.emailContato());
        assertEquals("Rua Nova, 1", res.endereco());
        assertEquals("11111-111", res.cep());
        assertEquals(0, new BigDecimal("-22").compareTo(res.latitude()));
        assertEquals(0, new BigDecimal("-47").compareTo(res.longitude()));
    }

    @Test
    void naoDeveMudarOQueNaoFoiEnviado() {
        AbrigoResponse abrigo = service.cadastrarAbrigo(cadastro(emailUnico("abrigo"), "Casa " + token(), null, null));

        AbrigoResponse res = service.atualizarAbrigo(abrigo.id(),
                new AbrigoRequest(null, "   ", null, null, null, null, null, null, null, null));

        assertEquals(abrigo.nome(), res.nome());
        assertEquals("Rua A, 10", res.endereco());
    }
}