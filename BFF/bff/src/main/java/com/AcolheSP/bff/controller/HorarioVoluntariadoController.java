package com.AcolheSP.bff.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.AcolheSP.bff.util.DataSanitizer;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/instituicoes/{instituicaoId}/horarios-voluntariado")
public class HorarioVoluntariadoController {
    private final WebClient backendWebClient;

    public HorarioVoluntariadoController(WebClient backendWebClient) {
        this.backendWebClient = backendWebClient;
    }

    private String base(Integer instituicaoId) {
        return "/api/instituicoes/" + instituicaoId + "/horarios-voluntariado";
    }

    @GetMapping
    public Mono<Object> listar(@PathVariable Integer instituicaoId) {
        return backendWebClient.get().uri(base(instituicaoId)).retrieve().bodyToMono(Object.class);
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> criar(@PathVariable Integer instituicaoId,
            @RequestBody Map<String, Object> dados) {
        return backendWebClient.post().uri(base(instituicaoId)).bodyValue(DataSanitizer.limparMap(dados))
                .retrieve().toEntity(Object.class);
    }

    @PutMapping("/{horarioId}")
    public Mono<Object> atualizar(@PathVariable Integer instituicaoId, @PathVariable Integer horarioId,
            @RequestBody Map<String, Object> dados) {
        return backendWebClient.put().uri(base(instituicaoId) + "/" + horarioId)
                .bodyValue(DataSanitizer.limparMap(dados)).retrieve().bodyToMono(Object.class);
    }

    @DeleteMapping("/{horarioId}")
    public Mono<ResponseEntity<Void>> remover(@PathVariable Integer instituicaoId, @PathVariable Integer horarioId) {
        return backendWebClient.delete().uri(base(instituicaoId) + "/" + horarioId)
                .retrieve().toBodilessEntity();
    }
}
