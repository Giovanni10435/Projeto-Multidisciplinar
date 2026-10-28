package com.AcolheSP.demo.dto;

import java.util.List;

/** Resultado de um abrigo na execução de Dijkstra sobre o grafo de proximidade. */
public record AbrigoDijkstraResponse(
        AbrigoMapaResponse abrigo,
        Double distanciaGrafoKm,
        List<Integer> caminhoIds) {
}
