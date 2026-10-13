package com.AcolheSP.demo.dto;

// Cadastro de voluntário/usuário (RF01) - role "usuario"
public record VoluntarioRequest(
        String email,
        String nome,
        String telefone,
        String disponibilidade) {
}
