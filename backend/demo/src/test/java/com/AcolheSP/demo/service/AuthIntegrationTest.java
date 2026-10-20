package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.LoginRequest;
import com.AcolheSP.demo.dto.LoginResponse;
import com.AcolheSP.demo.dto.VoluntarioRequest;
import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.repository.ContaRepository;

@SpringBootTest
@Transactional
class AuthIntegrationTest {

    @Autowired
    private VoluntarioService voluntarioService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ContaRepository contaRepo;

    private String emailUnico() {
        return "login." + UUID.randomUUID() + "@exemplo.com";
    }

    private HttpStatus statusDoErro(String email, String senha) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest(email, senha)));
        return HttpStatus.valueOf(ex.getStatusCode().value());
    }

    @Test
    void deveLogarDepoisDeCadastrarVoluntarioComSenha() {
        String email = emailUnico();
        var criado = voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));

        LoginResponse res = authService.login(new LoginRequest(email, "senha123"));

        assertEquals(criado.id(), res.id());
        assertEquals(email, res.email());
        assertEquals("usuario", res.role());
        assertEquals("Ana", res.nome());
    }

    @Test
    void naoDeveGuardarASenhaEmTextoPuro() {
        String email = emailUnico();
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));

        Conta conta = contaRepo.findByEmail(email).orElseThrow();

        assertNotNull(conta.getSenhaHash());
        assertEquals(false, conta.getSenhaHash().contains("senha123"));
    }

    @Test
    void naoDeveLogarComSenhaErrada() {
        String email = emailUnico();
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));

        assertEquals(HttpStatus.UNAUTHORIZED, statusDoErro(email, "errada"));
    }

    @Test
    void naoDeveLogarComEmailInexistente() {
        assertEquals(HttpStatus.UNAUTHORIZED, statusDoErro(emailUnico(), "senha123"));
    }

    @Test
    void naoDeveLogarContaCadastradaSemSenha() {
        String email = emailUnico();
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null));

        assertEquals(HttpStatus.UNAUTHORIZED, statusDoErro(email, "senha123"));
    }

    @Test
    void naoDeveLogarContaInativa() {
        String email = emailUnico();
        voluntarioService.cadastrar(new VoluntarioRequest(email, "Ana", null, null, "senha123"));
        Conta conta = contaRepo.findByEmail(email).orElseThrow();
        conta.setAtivo(false);
        contaRepo.saveAndFlush(conta);

        assertEquals(HttpStatus.FORBIDDEN, statusDoErro(email, "senha123"));
    }
}