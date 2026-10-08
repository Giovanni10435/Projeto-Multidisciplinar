package com.AcolheSP.bff.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.bff.service.AbrigoService;

import reactor.core.publisher.Mono;



@RestController 
public class AbrigoController {
    private final AbrigoService abrigoService;

    public AbrigoController(AbrigoService abrigoService) {
        this.abrigoService = abrigoService;
    }

    // UC07: Limpa e repassa o abrigo pro backend cadastrar
    @PostMapping("/api/abrigos")
    public Mono<Object> cadastrarAbrigo(@RequestBody Map<String, Object> abrigoDados) {
        return abrigoService.cadastrarAbrigo(abrigoDados);
    }

    // UC01: Recebe lista e retorna, construindo o path conforme backend
    @GetMapping("/api/abrigos")
    public Mono<Object> listarAbrigos(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String necessidade
    ) {
        return abrigoService.listarAbrigos(nome, necessidade);
    }

    // UC02: Recebe detalhe sobre determinado abrigo e retorna para o frontend
    @GetMapping("/api/abrigos/{id}")
    public Mono<Object> detalharAbrigo(@PathVariable Integer id) {
        return abrigoService.detalharAbrigo(id);
    }

    // UC08: Atualizar informações do abrigo
    @PutMapping("/api/abrigos/{id}")
    public Mono<Object> atualizarAbrigo(
        @PathVariable Integer id, 
        @RequestBody Map<String, Object> abrigoDados
    ) {
        abrigoDados.put("id", id);
        return abrigoService.atualizarAbrigo(abrigoDados);
    }
    
}
