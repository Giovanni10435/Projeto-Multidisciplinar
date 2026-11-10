package com.AcolheSP.bff.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.bff.dto.LoginRequest;
import com.AcolheSP.bff.dto.LoginResponse;
import com.AcolheSP.bff.dto.VoluntarioRequest;
import com.AcolheSP.bff.dto.VoluntarioResponse;
import com.AcolheSP.bff.util.DataSanitizer;

import reactor.core.publisher.Mono;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service 
public class UserService {
    private final WebClient backendWebClient;
    private final ObjectMapper objectMapper;

    public UserService(WebClient backendWebClient, ObjectMapper objectMapper) {
        this.backendWebClient = backendWebClient;
        this.objectMapper = objectMapper;
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

    // RF01: Cadastrar voluntário (usuário)
    public Mono<VoluntarioResponse> cadastrarUsuario(VoluntarioRequest dados) {
        Map<String, Object> dadosLimpos = DataSanitizer.limparMap(
            objectMapper.convertValue(
                dados,
                new TypeReference<Map<String, Object>>() {}
            )
        );
        // Credenciais precisam permanecer identicas no cadastro e no login.
        dadosLimpos.put("senha", dados.senha());

        return backendWebClient
            .post()
            .uri("/api/voluntarios")
            .bodyValue(dadosLimpos)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response ->
                response.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> Mono.error(new ResponseStatusException(
                        response.statusCode(),
                        body.isEmpty() ? "Requisição inválida ao backend" : body
                    )))
            )
            .bodyToMono(VoluntarioResponse.class);
    }

    // RF02: Login voluntário (usuário)
    public Mono<LoginResponse> loginUsuario(LoginRequest dados) {
        return backendWebClient
        .post()
        .uri("/api/auth/login")
        .bodyValue(dados)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, response ->
            response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> new ResponseStatusException(
                    response.statusCode(),
                    "Não foi possível realizar o login"
                ))
        )
        .bodyToMono(LoginResponse.class);
    }
}
