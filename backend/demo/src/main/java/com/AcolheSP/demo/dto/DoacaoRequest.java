package com.AcolheSP.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

// UC05 - corpo do POST /api/doacoes. Aceita camelCase e snake_case.
public record DoacaoRequest(
        @JsonAlias("voluntario_id") Integer voluntarioId,
        @JsonAlias("necessidade_id") Integer necessidadeId,
        Integer quantidade) {
}
