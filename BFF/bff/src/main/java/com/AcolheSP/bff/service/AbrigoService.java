package com.AcolheSP.bff.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.AcolheSP.bff.util.DataSanitizer;

import reactor.core.publisher.Mono;

@Service 
public class AbrigoService {

    private final WebClient backendWebClient;

    public AbrigoService(WebClient backendWebClient) {
        this.backendWebClient = backendWebClient;
    }

    // UC07: Limpa e repassa o abrigo pro backend cadastrar
    public Mono<Object> cadastrarAbrigo(Map<String, Object> abrigoDados) {

        Map<String, Object> dadosLimpos = DataSanitizer.limparMap(abrigoDados);

        return backendWebClient
            .post()
            .uri("/api/abrigos")
            .bodyValue(dadosLimpos)
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC01: Recebe lista e retorna, construindo o path conforme backend
    public Mono<Object> listarAbrigos(String nome, String necessidade) {
        // Constrói a URL com query params
        return backendWebClient
            .get()
            .uri(builder -> builder
                .path("/api/abrigos")
                .queryParamIfPresent("nome", Optional.ofNullable(null))
                .queryParamIfPresent("necessidade", Optional.ofNullable(null))
                .build()
            )
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC02: Recebe detalhe sobre determinado abrigo e retorna para o frontend
    public Mono<Object> detalharAbrigo(Integer id) {
        return backendWebClient
            .get()
            .uri("/api/abrigos/" + id)
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC08: Atualizar informações do abrigo
    public Mono<Object> atualizarAbrigo(Map<String, Object> params) {
        Map<String, Object> dadosLimpos = DataSanitizer.limparMap(params);
        Integer id = (Integer) dadosLimpos.get("id");

        return backendWebClient
            .put()
            .uri("/api/abrigos/" + id)
            .bodyValue(dadosLimpos)
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC03: Listar abrigos no mapa, ordenados por proximidade quando lat/lng forem enviados
    public Mono<Object> listarAbrigosMapa(Double lat, Double lng) {
        return backendWebClient
            .get()
            .uri(builder -> builder
                .path("/api/local/abrigos")
                .queryParamIfPresent("lat", Optional.ofNullable(lat))
                .queryParamIfPresent("lng", Optional.ofNullable(lng))
                .build()
            )
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC11: Listar solicitações (doações + voluntariados) de uma instituição
    public Mono<Object> listarSolicitacoes(Integer instituicaoId) {
        return backendWebClient
            .get()
            .uri("/api/instituicoes/" + instituicaoId + "/solicitacoes")
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC11: Aprovar/recusar solicitação
    public Mono<Object> atualizarSolicitacao(Map<String, Object> solicitacaoDados) {
        Integer instituicaoId = (Integer) solicitacaoDados.get("instituicao_id");
        Integer solicitacaoId = (Integer) solicitacaoDados.get("id");
        
        return backendWebClient
            .put()
            .uri("/api/instituicoes/" + instituicaoId + "/solicitacoes/" + solicitacaoId)
            .bodyValue(solicitacaoDados)
            .retrieve()
            .bodyToMono(Object.class);
    }
}
