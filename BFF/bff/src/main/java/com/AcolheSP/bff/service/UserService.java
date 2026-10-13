package com.AcolheSP.bff.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.AcolheSP.bff.util.DataSanitizer;

import reactor.core.publisher.Mono;

@Service 
public class UserService {
    private final WebClient backendWebClient;

    public UserService(WebClient backendWebClient) {
        this.backendWebClient = backendWebClient;
    }

    // UC05: Realizar doação
    public Mono<Object> realizarDoacoes(Map<String, Object> doacaoDados) {
        Map<String, Object> dadosLimpos = DataSanitizer.limparMap(doacaoDados);

        return backendWebClient
            .post()
            .uri("/api/doacoes")
            .bodyValue(dadosLimpos)
            .retrieve()
            .bodyToMono(Object.class);
    }

    // UC06: Demonstrar interesse em voluntariado
    public Mono<Object> interesseVoluntario(Map<String, Object> voluntariadoDados) {
        Map<String, Object> dadosLimpos = DataSanitizer.limparMap(voluntariadoDados);
        Integer instituicaoId = (Integer) dadosLimpos.get("instituicao_id");
        
        return backendWebClient
            .post()
            .uri("/api/instituicoes/" + instituicaoId + "/voluntariados")
            .bodyValue(dadosLimpos)
            .retrieve()
            .bodyToMono(Object.class);
    }
}
