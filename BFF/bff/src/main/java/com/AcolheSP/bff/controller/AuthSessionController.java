package com.AcolheSP.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RestController
public class AuthSessionController {
    private final WebClient backendWebClient;

    public AuthSessionController(WebClient backendWebClient) {
        this.backendWebClient = backendWebClient;
    }

    @GetMapping("/api/auth/csrf")
    public Mono<Object> csrf() {
        return backendWebClient.get().uri("/api/auth/csrf").retrieve().bodyToMono(Object.class);
    }

    @GetMapping("/api/auth/me")
    public Mono<Object> me() {
        return backendWebClient.get().uri("/api/auth/me").retrieve().bodyToMono(Object.class);
    }

    @PostMapping("/api/auth/logout")
    public Mono<ResponseEntity<Void>> logout() {
        return backendWebClient.post().uri("/api/auth/logout").retrieve().toBodilessEntity();
    }
}
