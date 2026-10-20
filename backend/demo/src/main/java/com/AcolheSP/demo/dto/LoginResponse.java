package com.AcolheSP.demo.dto;

import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Voluntario;

public record LoginResponse(Integer id, String email, String role, String nome) {

    public static LoginResponse de(Conta c) {
        String nome = null;
        if (c instanceof Voluntario v) {
            nome = v.getNome();
        } else if (c instanceof Instituicao i) {
            nome = i.getNome();
        }
        return new LoginResponse(c.getId(), c.getEmail(), c.getRole(), nome);
    }
}