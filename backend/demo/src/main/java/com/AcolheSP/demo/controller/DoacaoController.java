package com.AcolheSP.demo.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.DoacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.service.DoacaoService;

@RestController
public class DoacaoController {

    private final DoacaoService doacaoService;

    public DoacaoController(DoacaoService doacaoService) {
        this.doacaoService = doacaoService;
    }

    // UC05: Realizar doação (mesma rota do BFF)
    @PostMapping("/api/doacoes")
    @PreAuthorize("@acesso.voluntario(authentication, #doacaoDados.voluntarioId())")
    public ResponseEntity<SolicitacaoResponse> realizarDoacao(@RequestBody DoacaoRequest doacaoDados) {
        var criada = doacaoService.realizarDoacao(doacaoDados);
        return ResponseEntity.created(URI.create("/api/doacoes/" + criada.id())).body(criada);
    }
}
