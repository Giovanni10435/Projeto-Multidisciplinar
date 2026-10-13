package com.AcolheSP.demo.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.VoluntarioRequest;
import com.AcolheSP.demo.dto.VoluntarioResponse;
import com.AcolheSP.demo.service.VoluntarioService;

@RestController
@RequestMapping("/api/voluntarios")
public class VoluntarioController {

    private final VoluntarioService voluntarioService;

    public VoluntarioController(VoluntarioService voluntarioService) {
        this.voluntarioService = voluntarioService;
    }

    // RF01: Cadastrar voluntário (usuário)
    @PostMapping
    public ResponseEntity<VoluntarioResponse> cadastrar(@RequestBody VoluntarioRequest dados) {
        var criado = voluntarioService.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/voluntarios/" + criado.id())).body(criado);
    }

    @GetMapping("/{id}")
    public VoluntarioResponse buscar(@PathVariable Integer id) {
        return voluntarioService.buscar(id);
    }
}
