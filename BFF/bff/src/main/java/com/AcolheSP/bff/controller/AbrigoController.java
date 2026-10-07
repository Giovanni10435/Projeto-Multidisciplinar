package com.AcolheSP.bff.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.bff.service.AbrigoService;

import reactor.core.publisher.Mono;

@RestController 
public class AbrigoController {
    private final AbrigoService abrigoService;

    public AbrigoController(AbrigoService abrigoService) {
        this.abrigoService = abrigoService;
    }

    // UC07: Limpa e repassa o abrigo pro backend cadastrar
    @PostMapping("/api/abrigos")
    public Mono<Object> cadastrarAbrigo(@RequestBody Map<String, Object> abrigoDados) {
        return abrigoService.cadastrarAbrigo(abrigoDados);
    }
}
