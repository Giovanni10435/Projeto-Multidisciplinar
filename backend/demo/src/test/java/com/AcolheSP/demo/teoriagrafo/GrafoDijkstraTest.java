package com.AcolheSP.demo.teoriagrafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.AcolheSP.demo.teoriagrafo.graphFinal.CaminhosMinimos.GrafoDijkstra;

class GrafoDijkstraTest {
    @Test
    void calculaMenorCaminhoEReconstruiOsVertices() {
        GrafoDijkstra grafo = new GrafoDijkstra(4);
        grafo.inserirArestaBidirecional(0, 1, 2.0);
        grafo.inserirArestaBidirecional(1, 2, 3.0);
        grafo.inserirArestaBidirecional(0, 2, 10.0);

        GrafoDijkstra.Resultado resultado = grafo.dijkstra(0);

        assertEquals(5.0, resultado.distanciaPara(2), 1e-9);
        assertEquals(List.of(0, 1, 2), resultado.caminhoPara(2));
        assertTrue(resultado.alcancavel(2));
    }

    @Test
    void representaVerticesDesconectadosComoInalcancaveis() {
        GrafoDijkstra grafo = new GrafoDijkstra(3);
        grafo.inserirArestaBidirecional(0, 1, 1.0);

        GrafoDijkstra.Resultado resultado = grafo.dijkstra(0);

        assertFalse(resultado.alcancavel(2));
        assertTrue(resultado.caminhoPara(2).isEmpty());
    }
}
