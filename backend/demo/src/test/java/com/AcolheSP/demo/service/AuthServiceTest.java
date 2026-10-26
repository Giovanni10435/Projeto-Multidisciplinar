package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.LoginRequest;
import com.AcolheSP.demo.dto.LoginResponse;
import com.AcolheSP.demo.dto.VoluntarioRequest;
import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.repository.ContaRepository;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private EntityManager em;

    private static String token() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID() + "@exemplo.com";
    }

    @Autowired
    private VoluntarioService voluntarioService;

    @Autowired
    private AbrigoService abrigoService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ContaRepository contaRepo;

    @Autowired
    private PasswordHasher hasher;

    private HttpStatus statusDoErro(String email, String senha) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest(email, senha)));
        return HttpStatus.valueOf(ex.getStatusCode().value());
    }

    @Test
    void deveLogarDepoisDeCadastrarVoluntarioComSenha() {
        String email = emailUnico("login");
        var criado = voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));

        LoginResponse res = authService.login(new LoginRequest(email, "senha123"));

        assertEquals(criado.id(), res.id());
        assertEquals(email, res.email());
        assertEquals("usuario", res.role());
        assertEquals("Ana", res.nome());
    }

    @Test
    void deveLogarAbrigoCadastradoComSenhaEDevolverONome() {
        String email = emailUnico("abrigo");
        abrigoService.cadastrarAbrigo(new AbrigoRequest(email, "Casa " + token(), null, null, null, null,
                "Rua A, 10", null, new BigDecimal("-23.55"), new BigDecimal("-46.63"), "senha123"));

        LoginResponse res = authService.login(new LoginRequest(email, "senha123"));

        assertEquals("abrigo", res.role());
        assertTrue(res.nome().startsWith("Casa "));
    }

    @Test
    void deveLogarAdminSemNome() {
        Conta admin = new Conta();
        admin.setEmail(emailUnico("admin"));
        admin.setRole("admin");
        admin.setSenhaHash(hasher.gerarHash("senha123"));
        em.persist(admin);
        em.flush();

        LoginResponse res = authService.login(new LoginRequest(admin.getEmail(), "senha123"));

        assertEquals("admin", res.role());
        assertNull(res.nome());
    }

    @Test
    void deveIgnorarEspacosNoEmail() {
        String email = emailUnico("login");
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));

        LoginResponse res = authService.login(new LoginRequest("  " + email + "  ", "senha123"));

        assertEquals(email, res.email());
    }

    @Test
    void naoDeveGuardarASenhaEmTextoPuro() {
        String email = emailUnico("login");
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));

        Conta conta = contaRepo.findByEmail(email).orElseThrow();

        assertNotNull(conta.getSenhaHash());
        assertNotEquals("senha123", conta.getSenhaHash());
    }

    @Test
    void naoDeveLogarSemEmailOuSemSenha() {
        assertEquals(HttpStatus.BAD_REQUEST, statusDoErro(null, "senha123"));
        assertEquals(HttpStatus.BAD_REQUEST, statusDoErro("   ", "senha123"));
        assertEquals(HttpStatus.BAD_REQUEST, statusDoErro("ana@exemplo.com", null));
        assertEquals(HttpStatus.BAD_REQUEST, statusDoErro("ana@exemplo.com", "  "));
        assertThrows(ResponseStatusException.class, () -> authService.login(null));
    }

    @Test
    void naoDeveLogarComSenhaErrada() {
        String email = emailUnico("login");
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));

        assertEquals(HttpStatus.UNAUTHORIZED, statusDoErro(email, "errada"));
    }

    @Test
    void naoDeveLogarComEmailInexistente() {
        assertEquals(HttpStatus.UNAUTHORIZED, statusDoErro(emailUnico("naoexiste"), "senha123"));
    }

    @Test
    void naoDeveLogarContaCadastradaSemSenha() {
        String email = emailUnico("login");
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null));

        assertEquals(HttpStatus.UNAUTHORIZED, statusDoErro(email, "senha123"));
    }

    @Test
    void naoDeveLogarContaInativa() {
        String email = emailUnico("login");
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));
        Conta conta = contaRepo.findByEmail(email).orElseThrow();
        conta.setAtivo(false);
        contaRepo.saveAndFlush(conta);

        assertEquals(HttpStatus.FORBIDDEN, statusDoErro(email, "senha123"));
    }

    @Test
    void contaInativaComSenhaErradaNaoDeveRevelarQueEstaInativa() {
        String email = emailUnico("login");
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));
        Conta conta = contaRepo.findByEmail(email).orElseThrow();
        conta.setAtivo(false);
        contaRepo.saveAndFlush(conta);

        assertEquals(HttpStatus.UNAUTHORIZED, statusDoErro(email, "errada"));
    }
}