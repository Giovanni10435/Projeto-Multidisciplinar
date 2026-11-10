package com.AcolheSP.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.SolicitacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.service.SolicitacaoService;

@RestController
@RequestMapping("/api/instituicoes/{instituicaoId}/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    // UC11: Listar solicitações (doações + voluntariados) do abrigo
    @GetMapping
    @PreAuthorize("@acesso.abrigo(authentication, #instituicaoId)")
    public List<SolicitacaoResponse> listar(@PathVariable Integer instituicaoId) {
        return solicitacaoService.listar(instituicaoId);
    }

    // UC11: Aprovar/recusar solicitação. Body: {"tipo":"DOACAO|VOLUNTARIADO","status":"APROVADA|RECUSADA|..."}
    @PutMapping("/{solicitacaoId}")
    @PreAuthorize("@acesso.abrigo(authentication, #instituicaoId)")
    public SolicitacaoResponse atualizar(
            @PathVariable Integer instituicaoId,
            @PathVariable Integer solicitacaoId,
            @RequestBody SolicitacaoRequest dados) {
        return solicitacaoService.atualizar(instituicaoId, solicitacaoId, dados);
    }
}
