package com.AcolheSP.demo.dto;

import com.AcolheSP.demo.model.Necessidade;

public record NecessidadeResponse(
        Integer id,
        String descricao,
        Integer quantidadeSolicitada,
        Integer quantidadeRecebida,
        String status,
        Integer instituicaoId) {

    public static NecessidadeResponse de(Necessidade n) {
        return new NecessidadeResponse(
                n.getId(), n.getDescricao(),
                n.getQuantidadeSolicitada(), n.getQuantidadeRecebida(),
                n.getStatus().name(),
                n.getInstituicao().getId());
    }
}
