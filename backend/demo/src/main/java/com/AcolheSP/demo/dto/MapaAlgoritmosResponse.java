package com.AcolheSP.demo.dto;

import java.util.List;

/** Resultados do mapa ordenados por Haversine, Dijkstra e coloração gulosa. */
public record MapaAlgoritmosResponse(
        List<AbrigoMapaResponse> porHaversine,
        List<AbrigoDijkstraResponse> porDijkstra,
        List<GrupoColoracaoResponse> porColoracao,
        String descricaoGrafo) {
}
