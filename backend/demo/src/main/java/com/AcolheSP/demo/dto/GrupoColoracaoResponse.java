package com.AcolheSP.demo.dto;

import java.util.List;

/** Grupo de abrigos que recebeu a mesma cor no algoritmo guloso. */
public record GrupoColoracaoResponse(int cor, List<AbrigoMapaResponse> abrigos) {
}
