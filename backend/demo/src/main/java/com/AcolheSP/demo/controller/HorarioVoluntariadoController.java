package com.AcolheSP.demo.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.AcolheSP.demo.dto.HorarioVoluntariadoRequest;
import com.AcolheSP.demo.dto.HorarioVoluntariadoResponse;
import com.AcolheSP.demo.service.HorarioVoluntariadoService;

@RestController
@RequestMapping("/api/instituicoes/{instituicaoId}/horarios-voluntariado")
public class HorarioVoluntariadoController {
    private final HorarioVoluntariadoService service;

    public HorarioVoluntariadoController(HorarioVoluntariadoService service) {
        this.service = service;
    }

    @GetMapping
    public List<HorarioVoluntariadoResponse> listar(@PathVariable Integer instituicaoId) {
        return service.listar(instituicaoId);
    }

    @PostMapping
    @PreAuthorize("@acesso.abrigo(authentication, #instituicaoId)")
    public ResponseEntity<HorarioVoluntariadoResponse> criar(@PathVariable Integer instituicaoId,
            @RequestBody HorarioVoluntariadoRequest dados) {
        var criado = service.criar(instituicaoId, dados);
        return ResponseEntity.created(URI.create("/api/instituicoes/" + instituicaoId
                + "/horarios-voluntariado/" + criado.id())).body(criado);
    }

    @PutMapping("/{horarioId}")
    @PreAuthorize("@acesso.abrigo(authentication, #instituicaoId)")
    public HorarioVoluntariadoResponse atualizar(@PathVariable Integer instituicaoId,
            @PathVariable Integer horarioId, @RequestBody HorarioVoluntariadoRequest dados) {
        return service.atualizar(instituicaoId, horarioId, dados);
    }

    @DeleteMapping("/{horarioId}")
    @PreAuthorize("@acesso.abrigo(authentication, #instituicaoId)")
    public ResponseEntity<Void> remover(@PathVariable Integer instituicaoId, @PathVariable Integer horarioId) {
        service.remover(instituicaoId, horarioId);
        return ResponseEntity.noContent().build();
    }
}
