package com.AcolheSP.demo.dto;

import java.math.BigDecimal;

// UC03 - abrigo para o mapa. distanciaKm só vem preenchida quando lat/lng do usuário são enviados.
public record AbrigoMapaResponse(
        Integer id,
        String nome,
        String endereco,
        BigDecimal latitude,
        BigDecimal longitude,
        String statusVerificacao,
        long necessidadesAbertas,
        Double distanciaKm
) {
}
