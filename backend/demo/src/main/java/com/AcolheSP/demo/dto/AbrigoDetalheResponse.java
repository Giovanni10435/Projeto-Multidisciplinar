package com.AcolheSP.demo.dto;

import java.util.List;

// UC02 - dados do abrigo + necessidades abertas (inclui UC04)
public record AbrigoDetalheResponse(
        AbrigoResponse abrigo,
        List<NecessidadeResponse> necessidades) {
}
