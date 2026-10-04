package com.AcolheSP.bff.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class BackendService {

    // Pega endereço do backend pelo config do webclient e define ele
    // no construtor do service, como WebClientConfig já configura o
    // WebClient, chama direto WebClient ao invés do WebClientConfig
    private final WebClient backendWebClient;

    public BackendService(WebClient backendWebClient) {
        this.backendWebClient = backendWebClient;
    }

    // Chama o backend
    public Mono<String> buscarMensagens() {
        return backendWebClient
                .get()
                .uri("/api/hello-world")
                .retrieve()
                .bodyToMono(String.class);
    }
    /*
    Este Mono representa zero ou um resultado que será obtido de forma
    reativa, nesse caso, uma resposta que eventualmente produzirá uma
    String em uma operação assíncrona/reativa
    */
}
