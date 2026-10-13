package com.AcolheSP.demo.dto;

// UC11 - corpo do PUT /api/instituicoes/{id}/solicitacoes/{solicitacaoId}
// tipo: DOACAO | VOLUNTARIADO (os ids das duas tabelas são independentes, então o tipo é obrigatório)
// status: APROVADO/APROVADA, RECUSADO/RECUSADA ou o status exato (CONFIRMADA, ENTREGUE, EM_ANDAMENTO...)
public record SolicitacaoRequest(
        String tipo,
        String status) {
}
