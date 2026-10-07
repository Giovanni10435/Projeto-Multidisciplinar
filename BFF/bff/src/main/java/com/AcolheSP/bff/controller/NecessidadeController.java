package com.AcolheSP.bff.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.bff.service.NecessidadeService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/instituicoes")
public class NecessidadeController {
    private final NecessidadeService necessidadeService;

    public NecessidadeController(NecessidadeService necessidadeService) {
        this.necessidadeService = necessidadeService;
    }

    // UC09: Criar Necessidade
    @PostMapping("/{instituicaoId}/necessidades")
    public Mono<Object> cadastrarNecessidade(
        @PathVariable Integer instituicaoId,
        @RequestBody Map<String, Object> necessidadeDados
    ) {    
        necessidadeDados.put("instituicao_id", instituicaoId);
        return necessidadeService.cadastrarNecessidade(necessidadeDados);
    }

    // UC10: Atualizar Necessidade
    @PutMapping("/{instituicaoId}/necessidades/{necessidadeId}")
    public Mono<Object> atualizarNecessidade(
        @PathVariable Integer instituicaoId,
        @PathVariable Integer necessidadeId,
        @RequestBody Map<String, Object> necessidadeDados
    ) {
        necessidadeDados.put("id", necessidadeId);
        necessidadeDados.put("instituicao_id", instituicaoId);
        return necessidadeService.atualizarNecessidade(necessidadeDados);
    }

    // Remover necessidade
    @DeleteMapping("/{instituicaoId}/necessidades/{necessidadeId}")
    public Mono<Object> removerNecessidade(
        @PathVariable Integer instituicaoId,
        @PathVariable Integer necessidadeId
    ) {
        Map<String, Object> params = Map.of(
            "instituicao_id", instituicaoId,
            "id", necessidadeId 
        );
        return necessidadeService.removerNecessidade(params);
    }
}
