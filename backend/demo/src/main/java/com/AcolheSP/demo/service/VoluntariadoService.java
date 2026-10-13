package com.AcolheSP.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.dto.VoluntariadoRequest;
import com.AcolheSP.demo.model.StatusVoluntariado;
import com.AcolheSP.demo.model.Voluntariado;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.VoluntariadoRepository;
import com.AcolheSP.demo.repository.VoluntarioRepository;

@Service
public class VoluntariadoService {

    private static final List<StatusVoluntariado> ATIVOS = List.of(
            StatusVoluntariado.PENDENTE, StatusVoluntariado.APROVADO, StatusVoluntariado.EM_ANDAMENTO);

    private final VoluntariadoRepository voluntariadoRepo;
    private final VoluntarioRepository voluntarioRepo;
    private final InstituicaoRepository instituicaoRepo;

    public VoluntariadoService(VoluntariadoRepository voluntariadoRepo, VoluntarioRepository voluntarioRepo,
            InstituicaoRepository instituicaoRepo) {
        this.voluntariadoRepo = voluntariadoRepo;
        this.voluntarioRepo = voluntarioRepo;
        this.instituicaoRepo = instituicaoRepo;
    }

    // UC06 - Demonstrar interesse em voluntariado (nasce PENDENTE; o abrigo aprova/recusa no UC11)
    @Transactional
    public SolicitacaoResponse demonstrarInteresse(Integer instituicaoId, VoluntariadoRequest req) {
        if (req.voluntarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: voluntarioId");
        }
        if (req.dataInicio() != null && req.dataFim() != null && req.dataFim().isBefore(req.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataFim não pode ser antes de dataInicio");
        }

        var inst = instituicaoRepo.findById(instituicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));
        var voluntario = voluntarioRepo.findById(req.voluntarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voluntário não encontrado"));

        if (voluntariadoRepo.existsByVoluntarioIdAndInstituicaoIdAndStatusIn(voluntario.getId(), instituicaoId, ATIVOS)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esse voluntário já tem uma candidatura ativa nesse abrigo");
        }

        // Contato e disponibilidade informados na candidatura atualizam o cadastro do voluntário
        if (req.telefone() != null && !req.telefone().isBlank()) {
            voluntario.setTelefone(req.telefone());
        }
        if (req.disponibilidade() != null && !req.disponibilidade().isBlank()) {
            voluntario.setDisponibilidade(req.disponibilidade());
        }

        var v = new Voluntariado();
        v.setVoluntario(voluntario);
        v.setInstituicao(inst);
        v.setDataInicio(req.dataInicio());
        v.setDataFim(req.dataFim());
        v.setStatus(StatusVoluntariado.PENDENTE);
        return SolicitacaoResponse.de(voluntariadoRepo.save(v));
    }
}
