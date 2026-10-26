package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.VoluntarioRequest;
import com.AcolheSP.demo.dto.VoluntarioResponse;
import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.repository.ContaRepository;

@SpringBootTest
@Transactional
class VoluntarioServiceTest {

    @Autowired
    private EntityManager em;

    private static String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID() + "@exemplo.com";
    }

    private void sincronizar() {
        em.flush();
        em.clear();
    }

    @Autowired
    private VoluntarioService service;

    @Autowired
    private ContaRepository contaRepo;

    @Autowired
    private PasswordHasher hasher;

    private ResponseStatusException cadastrarComErro(VoluntarioRequest req) {
        return assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
    }

    @Test
    void deveCadastrarVoluntarioComSenhaNoBanco() {
        String email = emailUnico("ana");

        VoluntarioResponse res = service.cadastrar(
                new VoluntarioRequest(email, "Ana", "(11) 99999-0000", "Sábados", "senha123"));

        assertEquals(email, res.email());
        assertEquals("Ana", res.nome());
        assertEquals("(11) 99999-0000", res.telefone());
        assertEquals("Sábados", res.disponibilidade());

        sincronizar();
        Conta conta = contaRepo.findByEmail(email).orElseThrow();
        assertEquals(res.id(), conta.getId());
        assertEquals("usuario", conta.getRole());
        assertTrue(conta.isAtivo());
        assertNotEquals("senha123", conta.getSenhaHash());
        assertTrue(hasher.confere("senha123", conta.getSenhaHash()));
    }

    @Test
    void deveCadastrarVoluntarioSemSenhaEDeixarHashNulo() {
        String email = emailUnico("ana");

        service.cadastrar(new VoluntarioRequest(email, "Ana", null, null));

        sincronizar();
        assertNull(contaRepo.findByEmail(email).orElseThrow().getSenhaHash());
    }

    @Test
    void naoDeveCadastrarSemEmail() {
        ResponseStatusException ex = cadastrarComErro(new VoluntarioRequest(null, "Ana", null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: email", ex.getReason());
    }

    @Test
    void naoDeveCadastrarSemNome() {
        ResponseStatusException ex = cadastrarComErro(new VoluntarioRequest(emailUnico("ana"), "  ", null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: nome", ex.getReason());
    }

    @Test
    void naoDeveCadastrarComEmailJaExistente() {
        String email = emailUnico("ana");
        service.cadastrar(new VoluntarioRequest(email, "Ana", null, null));

        ResponseStatusException ex = cadastrarComErro(new VoluntarioRequest(email, "Outra Ana", null, null));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void naoDeveCadastrarComSenhaCurta() {
        String email = emailUnico("ana");

        ResponseStatusException ex = cadastrarComErro(new VoluntarioRequest(email, "Ana", null, null, "123"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertFalse(contaRepo.existsByEmail(email));
    }

    @Test
    void deveBuscarVoluntarioExistente() {
        var criado = service.cadastrar(new VoluntarioRequest(emailUnico("ana"), "Ana", null, null));
        sincronizar();

        VoluntarioResponse res = service.buscar(criado.id());

        assertEquals(criado.id(), res.id());
        assertEquals("Ana", res.nome());
    }

    @Test
    void naoDeveBuscarVoluntarioInexistente() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.buscar(Integer.MAX_VALUE));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}