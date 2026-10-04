package com.AcolheSP.bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.bff.service.BackendService;

import reactor.core.publisher.Mono;

// RestController indica que essa classe possui endpoint HTTP que retorna dados
@RestController
public class BffController {
    private final BackendService backendService;

    public BffController(BackendService backendService) {
        this.backendService = backendService;
    }

    @GetMapping("/mensagens")
    public Mono<String> buscarMensagens() {
        return backendService.buscarMensagens();
    }
}
