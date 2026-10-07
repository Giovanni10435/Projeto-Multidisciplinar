package com.AcolheSP.bff.service;

import java.util.Map;

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


}
