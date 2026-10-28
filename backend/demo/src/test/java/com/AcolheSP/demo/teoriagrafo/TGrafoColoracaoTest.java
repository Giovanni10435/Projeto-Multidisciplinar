package com.AcolheSP.demo.teoriagrafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.AcolheSP.demo.teoriagrafo.graphFinal.coloracao.TGrafoColoracao;

class TGrafoColoracaoTest {
    @Test
    void atribuiUmaCorPorVerticeESepraVerticesAdjacentes() {
        TGrafoColoracao grafo = new TGrafoColoracao(0);
        for (int i = 0; i < 4; i++) grafo.insereV("0 0", "V" + i);
        grafo.insereA(0, 1, 1.0f);
        grafo.insereA(1, 2, 1.0f);
        grafo.insereA(2, 0, 1.0f);
        grafo.insereA(2, 3, 1.0f);

        List<List<Integer>> grupos = grafo.coloracaoPorClasse();
        Map<Integer, Integer> corPorVertice = new HashMap<>();
        Set<Integer> vertices = new HashSet<>();
        for (int cor = 0; cor < grupos.size(); cor++) {
            for (int vertice : grupos.get(cor)) {
                assertTrue(vertices.add(vertice), "O vértice não deve receber mais de uma cor");
                corPorVertice.put(vertice, cor);
            }
        }
        assertEquals(Set.of(0, 1, 2, 3), vertices);
        assertNotEquals(corPorVertice.get(0), corPorVertice.get(1));
        assertNotEquals(corPorVertice.get(1), corPorVertice.get(2));
        assertNotEquals(corPorVertice.get(2), corPorVertice.get(0));
        assertNotEquals(corPorVertice.get(2), corPorVertice.get(3));
    }
}
