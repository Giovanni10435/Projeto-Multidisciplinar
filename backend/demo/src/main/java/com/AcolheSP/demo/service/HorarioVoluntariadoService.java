package com.AcolheSP.demo.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.AcolheSP.demo.dto.HorarioVoluntariadoRequest;
import com.AcolheSP.demo.dto.HorarioVoluntariadoResponse;
import com.AcolheSP.demo.model.HorarioVoluntariado;
import com.AcolheSP.demo.repository.HorarioVoluntariadoRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;

@Service
@Transactional
public class HorarioVoluntariadoService {
    private static final List<String> DIAS = List.of("Segunda-feira", "Terça-feira", "Quarta-feira",
            "Quinta-feira", "Sexta-feira", "Sábado", "Domingo");
    private final HorarioVoluntariadoRepository horarios;
    private final InstituicaoRepository instituicoes;

    public HorarioVoluntariadoService(HorarioVoluntariadoRepository horarios, InstituicaoRepository instituicoes) {
        this.horarios = horarios;
        this.instituicoes = instituicoes;
    }

    @Transactional(readOnly = true)
    public List<HorarioVoluntariadoResponse> listar(Integer instituicaoId) {
        if (!instituicoes.existsById(instituicaoId)) throw inexistente("Abrigo");
        return horarios.findByInstituicaoIdAndAtivoTrue(instituicaoId).stream()
                .sorted(Comparator.comparingInt((HorarioVoluntariado h) -> DIAS.indexOf(h.getDia()))
                        .thenComparing(HorarioVoluntariado::getInicio).thenComparing(HorarioVoluntariado::getId))
                .map(HorarioVoluntariadoResponse::de).toList();
    }

    public HorarioVoluntariadoResponse criar(Integer instituicaoId, HorarioVoluntariadoRequest req) {
        var instituicao = instituicoes.findById(instituicaoId).orElseThrow(() -> inexistente("Abrigo"));
        var horario = new HorarioVoluntariado();
        horario.setInstituicao(instituicao);
        preencher(horario, req);
        return HorarioVoluntariadoResponse.de(horarios.save(horario));
    }

    public HorarioVoluntariadoResponse atualizar(Integer instituicaoId, Integer id, HorarioVoluntariadoRequest req) {
        instituicoes.buscarComBloqueio(instituicaoId).orElseThrow(() -> inexistente("Abrigo"));
        var horario = buscar(instituicaoId, id);
        if (!horario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário removido não pode ser alterado");
        }
        preencher(horario, req);
        return HorarioVoluntariadoResponse.de(horarios.save(horario));
    }

    public void remover(Integer instituicaoId, Integer id) {
        instituicoes.buscarComBloqueio(instituicaoId).orElseThrow(() -> inexistente("Abrigo"));
        var horario = buscar(instituicaoId, id);
        // Exclusao logica preserva as candidaturas e impede novos interesses.
        horario.setAtivo(false);
        horarios.save(horario);
    }

    private HorarioVoluntariado buscar(Integer instituicaoId, Integer id) {
        return horarios.findByIdAndInstituicaoId(id, instituicaoId).orElseThrow(() -> inexistente("Horário"));
    }

    private static void preencher(HorarioVoluntariado h, HorarioVoluntariadoRequest req) {
        if (req == null || req.dia() == null || !DIAS.contains(req.dia().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dia deve ser um dia da semana em português");
        }
        if (req.funcao() == null || req.funcao().isBlank() || req.funcao().trim().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "funcao deve ter entre 1 e 150 caracteres");
        }
        if (req.inicio() == null || req.fim() == null || !req.fim().isAfter(req.inicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fim deve ser depois de inicio no mesmo dia");
        }
        h.setDia(req.dia().trim());
        h.setFuncao(req.funcao().trim());
        h.setInicio(req.inicio());
        h.setFim(req.fim());
    }

    private static ResponseStatusException inexistente(String recurso) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, recurso + " não encontrado nesse abrigo");
    }
}
