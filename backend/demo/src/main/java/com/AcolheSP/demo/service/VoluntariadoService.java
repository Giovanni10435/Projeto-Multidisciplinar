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
import com.AcolheSP.demo.repository.HorarioVoluntariadoRepository;
import com.AcolheSP.demo.repository.VoluntariadoRepository;
import com.AcolheSP.demo.repository.VoluntarioRepository;

@Service
public class VoluntariadoService {

    private static final List<StatusVoluntariado> ATIVOS = List.of(
            StatusVoluntariado.PENDENTE, StatusVoluntariado.APROVADO, StatusVoluntariado.EM_ANDAMENTO);

    private final VoluntariadoRepository voluntariadoRepo;
    private final VoluntarioRepository voluntarioRepo;
    private final InstituicaoRepository instituicaoRepo;
    private final HorarioVoluntariadoRepository horarioRepo;

    public VoluntariadoService(VoluntariadoRepository voluntariadoRepo, VoluntarioRepository voluntarioRepo,
            InstituicaoRepository instituicaoRepo, HorarioVoluntariadoRepository horarioRepo) {
        this.voluntariadoRepo = voluntariadoRepo;
        this.voluntarioRepo = voluntarioRepo;
        this.instituicaoRepo = instituicaoRepo;
        this.horarioRepo = horarioRepo;
    }

    // UC06 - Demonstrar interesse em voluntariado (nasce PENDENTE; o abrigo aprova/recusa no UC11)
    @Transactional
    public SolicitacaoResponse demonstrarInteresse(Integer instituicaoId, VoluntariadoRequest req) {
        if (req == null || req.voluntarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: voluntarioId");
        }
        if (req.dataInicio() != null && req.dataFim() != null && req.dataFim().isBefore(req.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataFim não pode ser antes de dataInicio");
        }

        var inst = instituicaoRepo.buscarComBloqueio(instituicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));
        var voluntario = voluntarioRepo.findById(req.voluntarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voluntário não encontrado"));

        var horario = req.horarioId() == null ? null : horarioRepo
                .findByIdAndInstituicaoId(req.horarioId(), instituicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horário não encontrado nesse abrigo"));
        if (horario != null && !horario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário não está mais disponível");
        }
        if (!voluntario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voluntário inativo");
        }
        boolean duplicada = horario == null
                ? voluntariadoRepo.existsByVoluntarioIdAndInstituicaoIdAndStatusIn(voluntario.getId(), instituicaoId, ATIVOS)
                : voluntariadoRepo.existsByVoluntarioIdAndHorarioIdAndStatusIn(voluntario.getId(), horario.getId(), ATIVOS);
        if (duplicada) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esse voluntário já tem uma candidatura ativa nesse abrigo");
        }

        if ((req.telefone() != null && req.telefone().length() > 20)
                || (req.disponibilidade() != null && req.disponibilidade().length() > 255)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "telefone ou disponibilidade excede o limite");
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
        v.setHorario(horario);
        v.setDataInicio(req.dataInicio());
        v.setDataFim(req.dataFim());
        v.setStatus(StatusVoluntariado.PENDENTE);
        return SolicitacaoResponse.de(voluntariadoRepo.save(v));
    }
}
