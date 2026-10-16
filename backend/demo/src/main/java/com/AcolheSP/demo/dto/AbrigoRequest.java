package com.AcolheSP.demo.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAlias;

// UC07 - corpo do POST /api/abrigos (o BFF repassa o JSON do front).
// Aceita camelCase e snake_case (ex.: emailContato ou email_contato).
public record AbrigoRequest (
        String email,
        String nome,
        String cnpj,
        String descricao,
        String telefone,
        @JsonAlias("email_contato") String emailContato,
        String endereco,
        String cep,
        BigDecimal latitude,
        BigDecimal longitude) {
}
