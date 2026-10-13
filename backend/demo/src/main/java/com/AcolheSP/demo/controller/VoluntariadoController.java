package com.AcolheSP.demo.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.dto.VoluntariadoRequest;
import com.AcolheSP.demo.service.VoluntariadoService;

@RestController
public class VoluntariadoController {

    private final VoluntariadoService voluntariadoService;

    public VoluntariadoController(VoluntariadoService voluntariadoService) {
        this.voluntariadoService = voluntariadoService;
    }

    // UC06: Demonstrar interesse em voluntariado (mesma rota do BFF)
    @PostMapping("/api/instituicoes/{instituicaoId}/voluntariados")
    public ResponseEntity<SolicitacaoResponse> demonstrarInteresse(
            @PathVariable Integer instituicaoId,
            @RequestBody VoluntariadoRequest voluntariadoDados) {
        var criado = voluntariadoService.demonstrarInteresse(instituicaoId, voluntariadoDados);
        return ResponseEntity
                .created(URI.create("/api/instituicoes/" + instituicaoId + "/voluntariados/" + criado.id()))
                .body(criado);
    }
}
