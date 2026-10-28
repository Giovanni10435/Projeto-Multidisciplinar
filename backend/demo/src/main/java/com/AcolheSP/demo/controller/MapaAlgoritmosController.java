package com.AcolheSP.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.MapaAlgoritmosResponse;
import com.AcolheSP.demo.service.MapaAlgoritmosService;

@RestController
public class MapaAlgoritmosController {

    private final MapaAlgoritmosService algoritmosService;

    public MapaAlgoritmosController(MapaAlgoritmosService algoritmosService) {
        this.algoritmosService = algoritmosService;
    }

    /**
     * Executa os três resultados do mapa sem mudar o endpoint original.
     * Ex.: /api/local/abrigos/algoritmos?lat=-23.55&lng=-46.63
     */
    @GetMapping("/api/local/abrigos/algoritmos")
    public MapaAlgoritmosResponse calcularAlgoritmos(
            @RequestParam Double lat,
            @RequestParam Double lng) {
        return algoritmosService.calcularAlgoritmos(lat, lng);
    }
}
