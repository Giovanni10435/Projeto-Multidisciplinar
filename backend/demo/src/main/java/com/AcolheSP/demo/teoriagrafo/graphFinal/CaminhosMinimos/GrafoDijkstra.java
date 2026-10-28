package com.AcolheSP.demo.teoriagrafo.graphFinal.CaminhosMinimos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Grafo ponderado não direcionado e algoritmo de Dijkstra.
 * Os pesos devem ser não negativos (no mapa, são distâncias Haversine em km).
 */
public final class GrafoDijkstra {

    private final List<List<Aresta>> adjacencias;

    public GrafoDijkstra(int quantidadeVertices) {
        if (quantidadeVertices < 0) {
            throw new IllegalArgumentException("A quantidade de vértices não pode ser negativa");
        }
        adjacencias = new ArrayList<>(quantidadeVertices);
        for (int i = 0; i < quantidadeVertices; i++) {
            adjacencias.add(new ArrayList<>());
        }
    }

    public int quantidadeVertices() {
        return adjacencias.size();
    }

    /** Insere uma aresta bidirecional. A distância deve ser finita e não negativa. */
    public void inserirArestaBidirecional(int origem, int destino, double peso) {
        validarVertice(origem);
        validarVertice(destino);
        if (origem == destino) {
            return;
        }
        if (!Double.isFinite(peso) || peso < 0) {
            throw new IllegalArgumentException("O peso da aresta deve ser finito e não negativo");
        }
        adjacencias.get(origem).add(new Aresta(destino, peso));
        adjacencias.get(destino).add(new Aresta(origem, peso));
    }

    /** Calcula os menores caminhos a partir de um vértice usando fila de prioridade. */
    public Resultado dijkstra(int origem) {
        validarVertice(origem);

        int n = adjacencias.size();
        double[] distancias = new double[n];
        int[] anteriores = new int[n];
        Arrays.fill(distancias, Double.POSITIVE_INFINITY);
        Arrays.fill(anteriores, -1);
        distancias[origem] = 0.0;

        PriorityQueue<Estado> fila = new PriorityQueue<>(
                Comparator.comparingDouble(Estado::distancia).thenComparingInt(Estado::vertice));
        fila.add(new Estado(origem, 0.0));

        while (!fila.isEmpty()) {
            Estado estado = fila.poll();
            int atual = estado.vertice();

            // Ignora entradas antigas da fila, que já foram superadas por uma rota melhor.
            if (estado.distancia() > distancias[atual]) {
                continue;
            }

            for (Aresta aresta : adjacencias.get(atual)) {
                double novaDistancia = distancias[atual] + aresta.peso();
                if (novaDistancia < distancias[aresta.destino()]) {
                    distancias[aresta.destino()] = novaDistancia;
                    anteriores[aresta.destino()] = atual;
                    fila.add(new Estado(aresta.destino(), novaDistancia));
                }
            }
        }

        return new Resultado(origem, distancias, anteriores);
    }

    private void validarVertice(int vertice) {
        if (vertice < 0 || vertice >= adjacencias.size()) {
            throw new IllegalArgumentException("Índice de vértice inválido: " + vertice);
        }
    }

    private record Aresta(int destino, double peso) { }
    private record Estado(int vertice, double distancia) { }

    /** Resultado imutável de uma execução de Dijkstra. */
    public static final class Resultado {
        private final int origem;
        private final double[] distancias;
        private final int[] anteriores;

        private Resultado(int origem, double[] distancias, int[] anteriores) {
            this.origem = origem;
            this.distancias = distancias.clone();
            this.anteriores = anteriores.clone();
        }

        public int origem() {
            return origem;
        }

        public double distanciaPara(int destino) {
            validarDestino(destino);
            return distancias[destino];
        }

        public boolean alcancavel(int destino) {
            return Double.isFinite(distanciaPara(destino));
        }

        /** Retorna índices do vértice de origem até o destino, ou lista vazia se inalcançável. */
        public List<Integer> caminhoPara(int destino) {
            validarDestino(destino);
            if (!alcancavel(destino)) {
                return List.of();
            }

            List<Integer> invertido = new ArrayList<>();
            int atual = destino;
            int limite = anteriores.length + 1;
            while (atual != -1 && limite-- > 0) {
                invertido.add(atual);
                if (atual == origem) {
                    java.util.Collections.reverse(invertido);
                    return List.copyOf(invertido);
                }
                atual = anteriores[atual];
            }
            // Proteção contra cadeia de predecessores inválida.
            return List.of();
        }

        private void validarDestino(int destino) {
            if (destino < 0 || destino >= distancias.length) {
                throw new IllegalArgumentException("Índice de vértice inválido: " + destino);
            }
        }
    }
}
