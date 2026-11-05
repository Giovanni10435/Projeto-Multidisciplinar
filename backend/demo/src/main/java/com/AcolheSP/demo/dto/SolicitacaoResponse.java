package com.AcolheSP.demo.dto;

import java.time.LocalDate;

import com.AcolheSP.demo.model.Doacao;
import com.AcolheSP.demo.model.Voluntariado;
import com.AcolheSP.demo.model.Voluntario;

// UC11 - item único para doações e voluntariados. Campos que não se aplicam ao tipo vão null.
public record SolicitacaoResponse(
        Integer id,
        String tipo,
        String status,
        LocalDate data,
        Integer voluntarioId,
        String voluntarioNome,
        String voluntarioTelefone,
        String voluntarioDisponibilidade,
        Integer necessidadeId,
        String necessidadeDescricao,
        Integer quantidade,
        LocalDate dataInicio,
        LocalDate dataFim,
        Integer horarioId) {

    public SolicitacaoResponse(Integer id, String tipo, String status, LocalDate data,
            Integer voluntarioId, String voluntarioNome, String voluntarioTelefone,
            String voluntarioDisponibilidade, Integer necessidadeId, String necessidadeDescricao,
            Integer quantidade, LocalDate dataInicio, LocalDate dataFim) {
        this(id, tipo, status, data, voluntarioId, voluntarioNome, voluntarioTelefone,
                voluntarioDisponibilidade, necessidadeId, necessidadeDescricao, quantidade, dataInicio, dataFim, null);
    }

    public static SolicitacaoResponse de(Doacao d) {
        Voluntario v = d.getVoluntario();
        return new SolicitacaoResponse(
                d.getId(), "DOACAO", d.getStatus().name(), d.getData(),
                v.getId(), v.getNome(), v.getTelefone(), v.getDisponibilidade(),
                d.getNecessidade().getId(), d.getNecessidade().getDescricao(), d.getQuantidade(),
                null, null);
    }

    public static SolicitacaoResponse de(Voluntariado vt) {
        Voluntario v = vt.getVoluntario();
        return new SolicitacaoResponse(
                vt.getId(), "VOLUNTARIADO", vt.getStatus().name(), vt.getDataSolicitacao(),
                v.getId(), v.getNome(), v.getTelefone(), v.getDisponibilidade(),
                null, null, null,
                vt.getDataInicio(), vt.getDataFim(), vt.getHorario() == null ? null : vt.getHorario().getId());
    }
}
