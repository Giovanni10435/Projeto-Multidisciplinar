package com.AcolheSP.bff.dto;

public record VoluntarioRequest(
    String email,
    String nome,
    String senha,
    String telefone,
    String disponibilidade
) {}
