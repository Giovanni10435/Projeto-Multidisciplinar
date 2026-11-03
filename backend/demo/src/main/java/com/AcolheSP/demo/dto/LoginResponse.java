package com.AcolheSP.demo.dto;

import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Voluntario;

public record LoginResponse(Integer id, String email, String role, String nome, Integer instituicaoId) {

    // mantém os testes antigos (4 argumentos) compilando
    public LoginResponse(Integer id, String email, String role, String nome) {
        this(id, email, role, nome, null);
    }

    public static LoginResponse de(Conta c, Instituicao instituicao) {
        String nome = null;

        if (c instanceof Voluntario v) {
            nome = v.getNome();
        } else if (instituicao != null) {
            nome = instituicao.getNome();
        }

        Integer instituicaoId = instituicao != null ? instituicao.getId() : null;

        return new LoginResponse(c.getId(), c.getEmail(), c.getRole(), nome, instituicaoId);
    }
}