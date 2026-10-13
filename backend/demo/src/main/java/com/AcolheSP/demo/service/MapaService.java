package com.AcolheSP.demo.service;

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

@Service
public class MapaService {

    private static final double RAIO_TERRA_KM = 6371.0;

    private final InstituicaoRepository instituicaoRepo;
    private final NecessidadeRepository necessidadeRepo;

    public MapaService(InstituicaoRepository instituicaoRepo, NecessidadeRepository necessidadeRepo) {
        this.instituicaoRepo = instituicaoRepo;
        this.necessidadeRepo = necessidadeRepo;
    }

    // UC03 - Abrigos para o mapa. Sem lat/lng: ordem alfabética.
    // Com lat/lng do usuário: ordenados do mais perto pro mais longe (RF08).
    @Transactional(readOnly = true)
    public List<AbrigoMapaResponse> listarParaMapa(Double lat, Double lng) {
        if ((lat == null) != (lng == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie lat e lng juntas");
        }
        if (lat != null && (lat < -90 || lat > 90 || lng < -180 || lng > 180)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat/lng fora do intervalo válido");
        }

        List<AbrigoMapaResponse> lista = instituicaoRepo.findAll().stream()
                .filter(i -> i.getLocalizacao() != null)
                .map(i -> montar(i, lat, lng))
                .toList();

        Comparator<AbrigoMapaResponse> ordem = (lat == null)
                ? Comparator.comparing(AbrigoMapaResponse::nome, String.CASE_INSENSITIVE_ORDER)
                : Comparator.comparing(AbrigoMapaResponse::distanciaKm);

        return lista.stream().sorted(ordem).toList();
    }

    private AbrigoMapaResponse montar(Instituicao i, Double lat, Double lng) {
        var loc = i.getLocalizacao();
        Double distancia = null;
        if (lat != null) {
            distancia = arredondar(haversineKm(lat, lng,
                    loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue()));
        }
        return new AbrigoMapaResponse(
                i.getId(), i.getNome(), loc.getEndereco(),
                loc.getLatitude(), loc.getLongitude(),
                i.getStatusVerificacao().name(),
                necessidadeRepo.countByInstituicaoIdAndStatus(i.getId(), StatusNecessidade.ABERTA),
                distancia);
    }

    // Distância em linha reta entre dois pontos (km)
    static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RAIO_TERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
