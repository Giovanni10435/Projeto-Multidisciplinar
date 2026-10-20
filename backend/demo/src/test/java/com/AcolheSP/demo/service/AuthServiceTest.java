package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.LoginRequest;
import com.AcolheSP.demo.dto.LoginResponse;
import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Voluntario;
import com.AcolheSP.demo.repository.ContaRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ContaRepository contaRepo;

    private final PasswordHasher hasher = new PasswordHasher();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(contaRepo, hasher);
    }

    private Voluntario voluntario(String email, String senha, boolean ativo) {
        Voluntario v = new Voluntario();
        v.setId(1);
        v.setEmail(email);
        v.setRole("usuario");
        v.setNome("Ana");
        v.setSenhaHash(senha == null ? null : hasher.gerarHash(senha));
        v.setAtivo(ativo);
        return v;
    }

    private ResponseStatusException tentarLogin(String email, String senha) {
        return assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest(email, senha)));
    }

    @Test
    void deveLogarVoluntarioComCredenciaisValidas() {
        when(contaRepo.findByEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(voluntario("ana@exemplo.com", "senha123", true)));

        LoginResponse res = authService.login(new LoginRequest("ana@exemplo.com", "senha123"));

        assertEquals(1, res.id());
        assertEquals("ana@exemplo.com", res.email());
        assertEquals("usuario", res.role());
        assertEquals("Ana", res.nome());
    }

    @Test
    void deveLogarAbrigoEDevolverNomeDaInstituicao() {
        Instituicao inst = new Instituicao();
        inst.setId(2);
        inst.setEmail("abrigo@exemplo.com");
        inst.setRole("abrigo");
        inst.setNome("Casa Esperança");
        inst.setSenhaHash(hasher.gerarHash("senha123"));
        when(contaRepo.findByEmail("abrigo@exemplo.com")).thenReturn(Optional.of(inst));

        LoginResponse res = authService.login(new LoginRequest("abrigo@exemplo.com", "senha123"));

        assertEquals("abrigo", res.role());
        assertEquals("Casa Esperança", res.nome());
    }

    @Test
    void deveLogarAdminSemNome() {
        Conta admin = new Conta();
        admin.setId(3);
        admin.setEmail("admin@exemplo.com");
        admin.setRole("admin");
        admin.setSenhaHash(hasher.gerarHash("senha123"));
        when(contaRepo.findByEmail("admin@exemplo.com")).thenReturn(Optional.of(admin));

        LoginResponse res = authService.login(new LoginRequest("admin@exemplo.com", "senha123"));

        assertEquals("admin", res.role());
        assertNull(res.nome());
    }

    @Test
    void deveIgnorarEspacosNoEmail() {
        when(contaRepo.findByEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(voluntario("ana@exemplo.com", "senha123", true)));

        LoginResponse res = authService.login(new LoginRequest("  ana@exemplo.com  ", "senha123"));

        assertEquals("ana@exemplo.com", res.email());
    }

    @Test
    void naoDeveLogarSemEmail() {
        ResponseStatusException ex = tentarLogin(null, "senha123");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: email", ex.getReason());
    }

    @Test
    void naoDeveLogarComEmailEmBranco() {
        assertEquals(HttpStatus.BAD_REQUEST, tentarLogin("   ", "senha123").getStatusCode());
    }

    @Test
    void naoDeveLogarSemSenha() {
        ResponseStatusException ex = tentarLogin("ana@exemplo.com", null);
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: senha", ex.getReason());
    }

    @Test
    void naoDeveLogarComCorpoNulo() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void naoDeveLogarComEmailInexistente() {
        when(contaRepo.findByEmail("naoexiste@exemplo.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = tentarLogin("naoexiste@exemplo.com", "senha123");

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Email ou senha inválidos", ex.getReason());
    }

    @Test
    void naoDeveLogarComSenhaErrada() {
        when(contaRepo.findByEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(voluntario("ana@exemplo.com", "senha123", true)));

        ResponseStatusException ex = tentarLogin("ana@exemplo.com", "senhaErrada");

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Email ou senha inválidos", ex.getReason());
    }

    @Test
    void naoDeveLogarContaSemSenhaCadastrada() {
        when(contaRepo.findByEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(voluntario("ana@exemplo.com", null, true)));

        assertEquals(HttpStatus.UNAUTHORIZED, tentarLogin("ana@exemplo.com", "senha123").getStatusCode());
    }

    @Test
    void naoDeveLogarContaInativaMesmoComSenhaCorreta() {
        when(contaRepo.findByEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(voluntario("ana@exemplo.com", "senha123", false)));

        ResponseStatusException ex = tentarLogin("ana@exemplo.com", "senha123");

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Conta inativa", ex.getReason());
    }

    @Test
    void contaInativaComSenhaErradaNaoDeveRevelarQueEstaInativa() {
        when(contaRepo.findByEmail("ana@exemplo.com"))
                .thenReturn(Optional.of(voluntario("ana@exemplo.com", "senha123", false)));

        assertEquals(HttpStatus.UNAUTHORIZED, tentarLogin("ana@exemplo.com", "senhaErrada").getStatusCode());
    }
}