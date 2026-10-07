package com.AcolheSP.bff.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.AcolheSP.bff.util.DataSanitizer;

import reactor.core.publisher.Mono;

@Service 
public class NecessidadeService {
    private final WebClient backendWebClient;

    public NecessidadeService(WebClient backendWebClient) {
        this.backendWebClient = backendWebClient;
    }

    // UC09: Limpa e repassa a necessidade do abrigo pro backend
    public Mono<Object> cadastrarNecessidade(Map<String, Object> abrigoDados) {

        Map<String, Object> dadosLimpos = DataSanitizer.limparMap(abrigoDados);

        return backendWebClient
            .post()
            .uri("/api/instituicoes/" + dadosLimpos.get("instituicao_id") + "/necessidades")
            .bodyValue(dadosLimpos)
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC10: Atualizar Necessidade
    public Mono<Object> atualizarNecessidade(Map<String, Object> abrigoDados) {
        Map<String, Object> dadosLimpos = DataSanitizer.limparMap(abrigoDados);

        return backendWebClient
            .put()
            .uri("/api/instituicoes/" + dadosLimpos.get("instituicao_id") + "/necessidades/" + dadosLimpos.get("necessidadeId"))
            .bodyValue(dadosLimpos)
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC10: Atualizar Necessidade
    public Mono<Object> removerNecessidade(Map<String, Object> params) {
        return backendWebClient
            .delete()
            .uri("/api/instituicoes/" + params.get("instituicao_id") + "/necessidades/" + params.get("necessidadeId"))
            .retrieve()
            .bodyToMono(Object.class);
    }
}
