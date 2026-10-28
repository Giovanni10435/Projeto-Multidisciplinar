package com.AcolheSP.demo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoDijkstraResponse;
import com.AcolheSP.demo.dto.AbrigoMapaResponse;
import com.AcolheSP.demo.dto.GrupoColoracaoResponse;
import com.AcolheSP.demo.dto.MapaAlgoritmosResponse;
import com.AcolheSP.demo.teoriagrafo.graphFinal.CaminhosMinimos.GrafoDijkstra;
import com.AcolheSP.demo.teoriagrafo.graphFinal.coloracao.TGrafoColoracao;

/**
 * Adapter entre os algoritmos da pasta TeoriaGrafo e os abrigos reais do backend.
 * O mapa continua sendo a fonte de dados e os algoritmos operam sobre os mesmos vértices/arestas.
 */
@Service
public class MapaAlgoritmosService {

    private static final int QUANTIDADE_VIZINHOS = 3;
    private static final String DESCRICAO_GRAFO = "Grafo de proximidade não direcionado: cada abrigo seleciona até "
            + "3 vizinhos mais próximos; a união das seleções forma arestas bidirecionais ponderadas pela distância Haversine. A localização do usuário "
            + "é conectada aos até 3 abrigos mais próximos para iniciar Dijkstra. A distância resultante é do grafo "
            + "de proximidade e não representa uma rota por ruas.";

    private final MapaService mapaService;

    public MapaAlgoritmosService(MapaService mapaService) {
        this.mapaService = mapaService;
    }

    public MapaAlgoritmosResponse calcularAlgoritmos(Double latitudeUsuario, Double longitudeUsuario) {
        validarCoordenadas(latitudeUsuario, longitudeUsuario);

        List<AbrigoMapaResponse> abrigos = mapaService.listarParaMapa(latitudeUsuario, longitudeUsuario);
        if (abrigos.isEmpty()) {
            return new MapaAlgoritmosResponse(List.of(), List.of(), List.of(), DESCRICAO_GRAFO);
        }

        double[][] pesos = calcularPesos(abrigos);
        boolean[][] adjacencia = construirAdjacencia(pesos, abrigos);

        List<AbrigoDijkstraResponse> porDijkstra = executarDijkstra(
                abrigos, pesos, adjacencia, latitudeUsuario, longitudeUsuario);
        List<GrupoColoracaoResponse> porColoracao = executarColoracao(abrigos, pesos, adjacencia);

        return new MapaAlgoritmosResponse(
                List.copyOf(abrigos), List.copyOf(porDijkstra), List.copyOf(porColoracao), DESCRICAO_GRAFO);
    }

    private static void validarCoordenadas(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe lat e lng da localização do usuário para calcular os algoritmos");
        }
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat/lng fora do intervalo válido");
        }
    }

    private static double[][] calcularPesos(List<AbrigoMapaResponse> abrigos) {
        int n = abrigos.size();
        double[][] pesos = new double[n][n];
        for (int i = 0; i < n; i++) {
            AbrigoMapaResponse a = abrigos.get(i);
            for (int j = i + 1; j < n; j++) {
                AbrigoMapaResponse b = abrigos.get(j);
                double peso = MapaService.haversineKm(
                        a.latitude().doubleValue(), a.longitude().doubleValue(),
                        b.latitude().doubleValue(), b.longitude().doubleValue());
                pesos[i][j] = peso;
                pesos[j][i] = peso;
            }
        }
        return pesos;
    }

    /** Cada vértice seleciona até três vizinhos mais próximos; a união das escolhas é bidirecional. */
    private static boolean[][] construirAdjacencia(double[][] pesos, List<AbrigoMapaResponse> abrigos) {
        int n = abrigos.size();
        boolean[][] adjacencia = new boolean[n][n];
        for (int origem = 0; origem < n; origem++) {
            final int indiceOrigem = origem;
            List<Integer> candidatos = new ArrayList<>(Math.max(0, n - 1));
            for (int destino = 0; destino < n; destino++) {
                if (destino != origem) candidatos.add(destino);
            }
            candidatos.sort(Comparator
                    .comparingDouble((Integer destino) -> pesos[indiceOrigem][destino])
                    .thenComparing(destino -> nome(abrigos.get(destino)), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(destino -> abrigos.get(destino).id(), Comparator.nullsLast(Integer::compareTo)));

            int limite = Math.min(QUANTIDADE_VIZINHOS, candidatos.size());
            for (int i = 0; i < limite; i++) {
                int vizinho = candidatos.get(i);
                adjacencia[origem][vizinho] = true;
                adjacencia[vizinho][origem] = true;
            }
        }
        return adjacencia;
    }

    private static List<AbrigoDijkstraResponse> executarDijkstra(
            List<AbrigoMapaResponse> abrigos,
            double[][] pesos,
            boolean[][] adjacencia,
            double latitudeUsuario,
            double longitudeUsuario) {

        int n = abrigos.size();
        int origemVirtual = n;
        GrafoDijkstra grafo = new GrafoDijkstra(n + 1);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (adjacencia[i][j]) {
                    grafo.inserirArestaBidirecional(i, j, pesos[i][j]);
                }
            }
        }

        double[] distanciasDiretas = new double[n];
        List<Integer> maisProximos = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            AbrigoMapaResponse abrigo = abrigos.get(i);
            distanciasDiretas[i] = MapaService.haversineKm(
                    latitudeUsuario, longitudeUsuario,
                    abrigo.latitude().doubleValue(), abrigo.longitude().doubleValue());
            maisProximos.add(i);
        }
        maisProximos.sort(Comparator
                .comparingDouble((Integer i) -> distanciasDiretas[i])
                .thenComparing(i -> nome(abrigos.get(i)), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(i -> abrigos.get(i).id(), Comparator.nullsLast(Integer::compareTo)));

        // O usuário é um vértice virtual que liga aos até três abrigos mais próximos.
        int conexoesOrigem = Math.min(QUANTIDADE_VIZINHOS, n);
        for (int i = 0; i < conexoesOrigem; i++) {
            int abrigo = maisProximos.get(i);
            grafo.inserirArestaBidirecional(origemVirtual, abrigo, distanciasDiretas[abrigo]);
        }

        GrafoDijkstra.Resultado resultado = grafo.dijkstra(origemVirtual);
        List<AbrigoDijkstraResponse> resposta = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double distancia = resultado.distanciaPara(i);
            if (!Double.isFinite(distancia)) {
                resposta.add(new AbrigoDijkstraResponse(abrigos.get(i), null, List.of()));
                continue;
            }

            List<Integer> caminhoIds = resultado.caminhoPara(i).stream()
                    .filter(indice -> indice != origemVirtual)
                    .map(indice -> abrigos.get(indice).id())
                    .toList();
            resposta.add(new AbrigoDijkstraResponse(abrigos.get(i), arredondar(distancia), caminhoIds));
        }

        resposta.sort(Comparator
                .comparing(AbrigoDijkstraResponse::distanciaGrafoKm, Comparator.nullsLast(Double::compareTo))
                .thenComparing(r -> nome(r.abrigo()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(r -> r.abrigo().id(), Comparator.nullsLast(Integer::compareTo)));
        return resposta;
    }

    private static List<GrupoColoracaoResponse> executarColoracao(
            List<AbrigoMapaResponse> abrigos, double[][] pesos, boolean[][] adjacencia) {
        TGrafoColoracao grafo = new TGrafoColoracao(0);
        for (AbrigoMapaResponse abrigo : abrigos) {
            String coordenadas = abrigo.latitude().toPlainString() + " " + abrigo.longitude().toPlainString();
            grafo.insereV(coordenadas, abrigo.nome());
        }
        for (int i = 0; i < abrigos.size(); i++) {
            for (int j = i + 1; j < abrigos.size(); j++) {
                if (adjacencia[i][j]) {
                    grafo.insereA(i, j, (float) pesos[i][j]);
                }
            }
        }

        List<List<Integer>> grupos = grafo.coloracaoPorClasse();
        List<GrupoColoracaoResponse> resposta = new ArrayList<>(grupos.size());
        for (int cor = 0; cor < grupos.size(); cor++) {
            List<AbrigoMapaResponse> membros = grupos.get(cor).stream()
                    .map(abrigos::get)
                    .sorted(Comparator.comparing(MapaAlgoritmosService::nome, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(AbrigoMapaResponse::id, Comparator.nullsLast(Integer::compareTo)))
                    .toList();
            resposta.add(new GrupoColoracaoResponse(cor + 1, List.copyOf(membros)));
        }
        return resposta;
    }

    private static String nome(AbrigoMapaResponse abrigo) {
        return abrigo.nome() == null ? "" : abrigo.nome();
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
