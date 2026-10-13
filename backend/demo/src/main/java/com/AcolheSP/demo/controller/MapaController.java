package com.AcolheSP.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.AbrigoMapaResponse;
import com.AcolheSP.demo.service.MapaService;

@RestController
public class MapaController {

    private final MapaService mapaService;

    public MapaController(MapaService mapaService) {
        this.mapaService = mapaService;
    }

    // UC03: Abrigos no mapa. Ex.: /api/local/abrigos?lat=-23.55&lng=-46.63 (ordena por proximidade)
    @GetMapping("/api/local/abrigos")
    public List<AbrigoMapaResponse> listarParaMapa(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        return mapaService.listarParaMapa(lat, lng);
    }
}
