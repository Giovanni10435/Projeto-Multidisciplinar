package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void deveGerarHashQueNaoEhASenhaEConfereComEla() {
        String hash = hasher.gerarHash("senha123");
        assertNotEquals("senha123", hash);
        assertTrue(hasher.confere("senha123", hash));
    }

    @Test
    void naoDeveConferirSenhaErrada() {
        assertFalse(hasher.confere("outraSenha", hasher.gerarHash("senha123")));
    }

    @Test
    void deveGerarHashesDiferentesParaAMesmaSenha() {
        assertNotEquals(hasher.gerarHash("senha123"), hasher.gerarHash("senha123"));
    }

    @Test
    void naoDeveConferirComHashOuSenhaNulosOuVazios() {
        assertFalse(hasher.confere(null, "qualquer"));
        assertFalse(hasher.confere("senha123", null));
        assertFalse(hasher.confere("senha123", ""));
        assertFalse(hasher.confere("senha123", "   "));
    }

    @Test
    void naoDeveConferirComHashEmFormatoInvalido() {
        assertFalse(hasher.confere("senha123", "isso-nao-e-um-hash-bcrypt"));
    }

    @Test
    void hashDaNovaSenhaDeveRetornarNullParaSenhaVazia() {
        assertNull(hasher.hashDaNovaSenha(null));
        assertNull(hasher.hashDaNovaSenha(""));
        assertNull(hasher.hashDaNovaSenha("   "));
    }

    @Test
    void hashDaNovaSenhaDeveRejeitarSenhaCurta() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> hasher.hashDaNovaSenha("12345"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("senha deve ter ao menos 6 caracteres", ex.getReason());
    }

    @Test
    void hashDaNovaSenhaDeveGerarHashParaSenhaValida() {
        assertTrue(hasher.confere("123456", hasher.hashDaNovaSenha("123456")));
    }
}