package com.AcolheSP.demo.dto;

import java.math.BigDecimal;

import com.AcolheSP.demo.model.Instituicao;

public record AbrigoResponse(
        Integer id,
        String nome,
        String cnpj,
        String descricao,
        String telefone,
        String emailContato,
        String statusVerificacao,
        String endereco,
        String cep,
        BigDecimal latitude,
        BigDecimal longitude) {

    public static AbrigoResponse de(Instituicao i) {
        var loc = i.getLocalizacao();
        return new AbrigoResponse(
                i.getId(), i.getNome(), i.getCnpj(), i.getDescricao(),
                i.getTelefone(), i.getEmailContato(),
                i.getStatusVerificacao().name(),
                loc != null ? loc.getEndereco() : null,
                loc != null ? loc.getCep() : null,
                loc != null ? loc.getLatitude() : null,
                loc != null ? loc.getLongitude() : null);
    }
}
