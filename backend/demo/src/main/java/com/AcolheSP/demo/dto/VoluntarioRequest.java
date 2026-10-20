package com.AcolheSP.demo.dto;

public record VoluntarioRequest(
        String email,
        String nome,
        String telefone,
        String disponibilidade,
        String senha) {

    public VoluntarioRequest(String email, String nome, String telefone, String disponibilidade) {
        this(email, nome, telefone, disponibilidade, null);
    }
}