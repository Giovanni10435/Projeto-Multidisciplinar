package com.AcolheSP.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

// UC09 / UC10 - corpo do POST e do PUT de necessidades.
// Aceita camelCase e snake_case. Campos extras que o BFF manda (id, instituicao_id) são ignorados:
// quem manda é o id da URL.
public record NecessidadeRequest(
        String descricao,
        @JsonAlias({ "quantidade_solicitada", "quantidade" }) Integer quantidadeSolicitada,
        @JsonAlias("quantidade_recebida") Integer quantidadeRecebida,
        String status) {
}
