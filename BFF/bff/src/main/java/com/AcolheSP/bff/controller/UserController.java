package com.AcolheSP.bff.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.bff.dto.LoginRequest;
import com.AcolheSP.bff.dto.LoginResponse;
import com.AcolheSP.bff.dto.VoluntarioRequest;
import com.AcolheSP.bff.dto.VoluntarioResponse;
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

    // RF01: Cadastrar voluntário (usuário)
    @PostMapping("/api/voluntarios")
    public Mono<ResponseEntity<VoluntarioResponse>> cadastrarUsuario(@RequestBody VoluntarioRequest dados) {
        return userService.cadastrarUsuario(dados)
            .map(criado -> ResponseEntity
                .created(URI.create("/api/voluntarios/" + criado.id()))
                .body(criado));
    }
    
    // RF02: Login voluntário (usuário)
    @PostMapping("/api/auth/login")
    public Mono<LoginResponse> loginUsuario(@RequestBody LoginRequest dados) {
        return userService.loginUsuario(dados);
    }
    
}
