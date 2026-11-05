package com.AcolheSP.demo.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;

// UC06 - corpo do POST /api/instituicoes/{id}/voluntariados.
// Datas no formato yyyy-MM-dd. telefone/disponibilidade (opcionais) atualizam o cadastro do voluntário.
// O campo instituicao_id que o BFF manda junto é ignorado: vale o id da URL.
public record VoluntariadoRequest(
        @JsonAlias("voluntario_id") Integer voluntarioId,
        @JsonAlias("data_inicio") LocalDate dataInicio,
        @JsonAlias("data_fim") LocalDate dataFim,
        String telefone,
        String disponibilidade,
        @JsonAlias("horario_id") Integer horarioId) {
    public VoluntariadoRequest(Integer voluntarioId, LocalDate dataInicio, LocalDate dataFim,
            String telefone, String disponibilidade) {
        this(voluntarioId, dataInicio, dataFim, telefone, disponibilidade, null);
    }
}
