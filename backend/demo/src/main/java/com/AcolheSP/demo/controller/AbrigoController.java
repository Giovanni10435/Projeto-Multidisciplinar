package com.AcolheSP.demo.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.service.AbrigoService;

@RestController
public class AbrigoController {

    private final AbrigoService abrigoService;

    public AbrigoController(AbrigoService abrigoService) {
        this.abrigoService = abrigoService;
    }

    // UC07: Cadastrar abrigo (mesma rota do BFF)
    @PostMapping("/api/abrigos")
    public ResponseEntity<AbrigoResponse> cadastrarAbrigo(@RequestBody AbrigoRequest abrigoDados) {
        var criado = abrigoService.cadastrarAbrigo(abrigoDados);
        return ResponseEntity.created(URI.create("/api/abrigos/" + criado.id())).body(criado);
    }

    // Extra (para teste): buscar um abrigo
    @GetMapping("/api/abrigos/{id}")
    public AbrigoResponse buscarAbrigo(@PathVariable Integer id) {
        return abrigoService.buscarAbrigo(id);
    }
}
