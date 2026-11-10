package com.AcolheSP.demo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.NecessidadeRequest;
import com.AcolheSP.demo.dto.NecessidadeResponse;
import com.AcolheSP.demo.service.NecessidadeService;

@RestController
@RequestMapping("/api/instituicoes")
public class NecessidadeController {

    private final NecessidadeService necessidadeService;

    public NecessidadeController(NecessidadeService necessidadeService) {
        this.necessidadeService = necessidadeService;
    }

    // UC09: Criar Necessidade (mesma rota do BFF)
    @PostMapping("/{instituicaoId}/necessidades")
    @PreAuthorize("@acesso.abrigo(authentication, #instituicaoId)")
    public ResponseEntity<NecessidadeResponse> cadastrarNecessidade(
            @PathVariable Integer instituicaoId,
            @RequestBody NecessidadeRequest necessidadeDados) {
        var criada = necessidadeService.cadastrarNecessidade(instituicaoId, necessidadeDados);
        return ResponseEntity
                .created(URI.create("/api/instituicoes/" + instituicaoId + "/necessidades/" + criada.id()))
                .body(criada);
    }

    // UC10: Atualizar Necessidade
    @PutMapping("/{instituicaoId}/necessidades/{necessidadeId}")
    @PreAuthorize("@acesso.abrigo(authentication, #instituicaoId)")
    public NecessidadeResponse atualizarNecessidade(
            @PathVariable Integer instituicaoId,
            @PathVariable Integer necessidadeId,
            @RequestBody NecessidadeRequest necessidadeDados) {
        return necessidadeService.atualizarNecessidade(instituicaoId, necessidadeId, necessidadeDados);
    }

    // Remover necessidade
    @DeleteMapping("/{instituicaoId}/necessidades/{necessidadeId}")
    @PreAuthorize("@acesso.abrigo(authentication, #instituicaoId)")
    public ResponseEntity<Void> removerNecessidade(
            @PathVariable Integer instituicaoId,
            @PathVariable Integer necessidadeId) {
        necessidadeService.removerNecessidade(instituicaoId, necessidadeId);
        return ResponseEntity.noContent().build();
    }

    // Extra (para teste): listar necessidades de um abrigo
    @GetMapping("/{instituicaoId}/necessidades")
    public List<NecessidadeResponse> listarNecessidades(@PathVariable Integer instituicaoId) {
        return necessidadeService.listarNecessidades(instituicaoId);
    }
}
