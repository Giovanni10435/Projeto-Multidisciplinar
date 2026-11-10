package com.AcolheSP.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import at.favre.lib.crypto.bcrypt.BCrypt;

@Component
public class PasswordHasher {

    static final int CUSTO = 10;
    static final int TAMANHO_MINIMO = 6;

    public String gerarHash(String senha) {
        return BCrypt.withDefaults().hashToString(CUSTO, senha.toCharArray());
    }

    public boolean confere(String senha, String hash) {
        if (senha == null || hash == null || hash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.verifyer().verify(senha.toCharArray(), hash).verified;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    // Senha opcional no cadastro: vazia -> null (conta sem login). Se vier, mínimo 6 caracteres.
    public String hashDaNovaSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            return null;
        }
        if (senha.length() < TAMANHO_MINIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "senha deve ter ao menos " + TAMANHO_MINIMO + " caracteres");
        }
        if (senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "senha deve ter no máximo 72 bytes UTF-8");
        }
        return gerarHash(senha);
    }
}
