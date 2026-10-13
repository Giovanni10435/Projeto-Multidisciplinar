package com.AcolheSP.bff.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.bff.service.UserService;

import reactor.core.publisher.Mono;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // UC05: Realizar doação
    @PostMapping("/api/doacoes")
    public Mono<Object> realizarDoacao(
        @RequestBody Map<String, Object> doacaoDados
    ) {
        return userService.realizarDoacoes(doacaoDados);
    }

    // UC06: Demonstrar interesse em voluntariado
    @PostMapping("/api/instituicoes/{instituicaoId}/voluntariados")
    public Mono<Object> interesseVoluntario(
        @PathVariable Integer instituicaoId,
        @RequestBody Map<String, Object> voluntariadoDados
    ) {
        voluntariadoDados.put("insituicao_id", instituicaoId);
        return userService.interesseVoluntario(voluntariadoDados);
    }
    
    
    
}
