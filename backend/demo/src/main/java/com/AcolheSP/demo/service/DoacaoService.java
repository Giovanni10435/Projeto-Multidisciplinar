package com.AcolheSP.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.DoacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.model.Doacao;
import com.AcolheSP.demo.model.StatusDoacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.DoacaoRepository;
import com.AcolheSP.demo.repository.NecessidadeRepository;
import com.AcolheSP.demo.repository.VoluntarioRepository;

@Service
public class DoacaoService {

    private final DoacaoRepository doacaoRepo;
    private final VoluntarioRepository voluntarioRepo;
    private final NecessidadeRepository necessidadeRepo;

    public DoacaoService(DoacaoRepository doacaoRepo, VoluntarioRepository voluntarioRepo,
            NecessidadeRepository necessidadeRepo) {
        this.doacaoRepo = doacaoRepo;
        this.voluntarioRepo = voluntarioRepo;
        this.necessidadeRepo = necessidadeRepo;
    }

    // UC05 - Realizar doação (nasce PENDENTE; o abrigo confirma/recusa no UC11)
    @Transactional
    public SolicitacaoResponse realizarDoacao(DoacaoRequest req) {
        if (req == null || req.voluntarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: voluntarioId");
        }
        if (req.necessidadeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: necessidadeId");
        }
        if (req.quantidade() == null || req.quantidade() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantidade deve ser maior que zero");
        }

        var voluntario = voluntarioRepo.findById(req.voluntarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voluntário não encontrado"));
        var necessidade = necessidadeRepo.buscarComBloqueio(req.necessidadeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Necessidade não encontrada"));

        if (necessidade.getStatus() != StatusNecessidade.ABERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Essa necessidade não está mais aberta");
        }

        if (!voluntario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voluntário inativo");
        }
        long comprometida = doacaoRepo.quantidadeComprometida(necessidade.getId(),
                List.of(StatusDoacao.PENDENTE, StatusDoacao.CONFIRMADA));
        long disponivel = (long) necessidade.getQuantidadeSolicitada() - necessidade.getQuantidadeRecebida() - comprometida;
        if (req.quantidade() > disponivel) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "quantidade excede o saldo disponível da necessidade");
        }

        var d = new Doacao();
        d.setVoluntario(voluntario);
        d.setNecessidade(necessidade);
        d.setQuantidade(req.quantidade());
        d.setStatus(StatusDoacao.PENDENTE);
        return SolicitacaoResponse.de(doacaoRepo.save(d));
    }
}
