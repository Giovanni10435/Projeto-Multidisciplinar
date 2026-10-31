package com.AcolheSP.bff.dto;

public record VoluntarioResponse(
    Integer id,
    String email,
    String nome,
    String telefone,
    String disponibilidade
) {}
