package com.AcolheSP.demo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoMapaResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.NecessidadeRepository;
import com.AcolheSP.demo.utils.TGrafo;

@Service
public class MapaService {
    private final InstituicaoRepository instituicaoRepo;
    private final NecessidadeRepository necessidadeRepo;

    public MapaService(InstituicaoRepository instituicaoRepo, NecessidadeRepository necessidadeRepo) {
        this.instituicaoRepo = instituicaoRepo;
        this.necessidadeRepo = necessidadeRepo;
    }

    // UC03 - Abrigos para o mapa. 
    // Sem lat/lng: ordem alfabética.
    // Com lat/lng do usuário: monta grafo com usuário + abrigos e ordena por Dijkstra.
    @Transactional(readOnly = true)
    public List<AbrigoMapaResponse> listarParaMapa(Double lat, Double lng) {
        if ((lat == null) != (lng == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie lat e lng juntas");
        }

        if (lat != null && (lat < -90 || lat > 90 || lng < -180 || lng > 180)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat/lng fora do intervalo válido");
        }

        List<Instituicao> instituicoes = instituicaoRepo.findAll().stream()
                .filter(i -> i.getLocalizacao() != null)
                .toList();

        if (lat == null) {
            return instituicoes.stream()
                    .map(i -> montarSemDistancia(i))
                    .sorted(Comparator.comparing(
                            AbrigoMapaResponse::nome,
                            String.CASE_INSENSITIVE_ORDER
                    ))
                    .toList();
        }
        return calcularPorGrafo(instituicoes, lat, lng);
    }

    private List<AbrigoMapaResponse> calcularPorGrafo(
            List<Instituicao> instituicoes,
            double latUsuario,
            double lngUsuario
    ) {
        // Vértice 0 será sempre o usuário
        TGrafo grafo = new TGrafo(1);

        grafo.insereV(
                latUsuario + " " + lngUsuario,
                "Usuário"
        );

        List<Integer> indicesAbrigos = new ArrayList<>();

        for (Instituicao instituicao : instituicoes) {
            var loc = instituicao.getLocalizacao();

            int indice = grafo.insereV(
                    loc.getLatitude().doubleValue() + " "
                            + loc.getLongitude().doubleValue(),
                    instituicao.getNome()
            );

            indicesAbrigos.add(indice);
        }

        double[] distancias = grafo.dijkstra(0);

        List<AbrigoMapaResponse> resposta = new ArrayList<>();

        for (int i = 0; i < instituicoes.size(); i++) {
            Instituicao instituicao = instituicoes.get(i);
            int indiceAbrigo = indicesAbrigos.get(i);

            double distancia = distancias[indiceAbrigo];
            var loc = instituicao.getLocalizacao();

            // Fallback: abrigo isolado no grafo recebe a distância reta
            if (distancia == Double.MAX_VALUE) {
                distancia = grafo.calcularDistanciaKm(
                        latUsuario,
                        lngUsuario,
                        loc.getLatitude().doubleValue(),
                        loc.getLongitude().doubleValue()
                );
            }

            resposta.add(montarComDistancia(
                    instituicao,
                    loc.getLatitude(),
                    loc.getLongitude(),
                    arredondar(distancia)
            ));
        }

        return resposta.stream()
                .sorted(Comparator.comparing(AbrigoMapaResponse::distanciaKm))
                .toList();
    }

    private AbrigoMapaResponse montarSemDistancia(Instituicao i) {
        var loc = i.getLocalizacao();

        return new AbrigoMapaResponse(
            i.getId(),
            i.getNome(),
            loc.getEndereco(),
            loc.getLatitude(),
            loc.getLongitude(),
            i.getStatusVerificacao().name(),
            necessidadeRepo.countByInstituicaoIdAndStatus(
                i.getId(),
                StatusNecessidade.ABERTA
            ),
            null
        );
    }

    private AbrigoMapaResponse montarComDistancia(
        Instituicao i,
        java.math.BigDecimal latitude,
        java.math.BigDecimal longitude,
        double distancia
    ) {
        var loc = i.getLocalizacao();

        return new AbrigoMapaResponse(
                i.getId(),
                i.getNome(),
                loc.getEndereco(),
                latitude,
                longitude,
                i.getStatusVerificacao().name(),
                necessidadeRepo.countByInstituicaoIdAndStatus(
                        i.getId(),
                        StatusNecessidade.ABERTA
                ),
                distancia
        );
    }

    private static double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
