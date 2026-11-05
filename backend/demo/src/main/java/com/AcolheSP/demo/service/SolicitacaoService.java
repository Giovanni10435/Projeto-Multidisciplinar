package com.AcolheSP.demo.service;

import com.AcolheSP.demo.repository.NecessidadeRepository;
import java.util.Locale;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.SolicitacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.model.StatusDoacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.model.StatusVoluntariado;
import com.AcolheSP.demo.repository.DoacaoRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.VoluntariadoRepository;

// UC11 - Gerenciar solicitações (doações + voluntariados) de um abrigo
@Service
public class SolicitacaoService {

    private static final Set<StatusDoacao> DOACAO_FINAL = Set.of(StatusDoacao.ENTREGUE, StatusDoacao.CANCELADA);
    private static final Set<StatusVoluntariado> VOLUNTARIADO_FINAL = Set.of(
            StatusVoluntariado.RECUSADO, StatusVoluntariado.FINALIZADO, StatusVoluntariado.CANCELADO);

    private final DoacaoRepository doacaoRepo;
    private final VoluntariadoRepository voluntariadoRepo;
    private final InstituicaoRepository instituicaoRepo;
    private final NecessidadeRepository necessidadeRepo;

    public SolicitacaoService(DoacaoRepository doacaoRepo, VoluntariadoRepository voluntariadoRepo,
            InstituicaoRepository instituicaoRepo,
            NecessidadeRepository necessidadeRepo) {
        this.doacaoRepo = doacaoRepo;
        this.voluntariadoRepo = voluntariadoRepo;
        this.instituicaoRepo = instituicaoRepo;
        this.necessidadeRepo = necessidadeRepo;
    }

    // Lista tudo que o abrigo recebeu (mais recentes primeiro)
    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listar(Integer instituicaoId) {
        exigirInstituicao(instituicaoId);

        List<SolicitacaoResponse> todas = new ArrayList<>();
        doacaoRepo.findByNecessidadeInstituicaoId(instituicaoId).forEach(d -> todas.add(SolicitacaoResponse.de(d)));
        voluntariadoRepo.findByInstituicaoId(instituicaoId).forEach(v -> todas.add(SolicitacaoResponse.de(v)));

        todas.sort(Comparator
                .comparing(SolicitacaoResponse::data, Comparator.nullsLast(Comparator.<LocalDate>reverseOrder()))
                .thenComparing(SolicitacaoResponse::id, Comparator.<Integer>reverseOrder()));
        return todas;
    }

    // Aprova/recusa (ou avança o status de) uma solicitação
    @Transactional
    public SolicitacaoResponse atualizar(Integer instituicaoId, Integer solicitacaoId, SolicitacaoRequest req) {
        exigirInstituicao(instituicaoId);

        if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corpo obrigatório");
        String tipo = normalizar(req.tipo());
        if (tipo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Campo obrigatório: tipo (DOACAO ou VOLUNTARIADO)");
        }
        if (normalizar(req.status()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: status");
        }

        return switch (tipo) {
            case "DOACAO" -> atualizarDoacao(instituicaoId, solicitacaoId, req.status());
            case "VOLUNTARIADO" -> atualizarVoluntariado(instituicaoId, solicitacaoId, req.status());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "tipo inválido (use DOACAO ou VOLUNTARIADO)");
        };
    }

    private SolicitacaoResponse atualizarDoacao(Integer instituicaoId, Integer id, String statusTexto) {
        var d = doacaoRepo.findByIdAndNecessidadeInstituicaoId(id, instituicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Doação não encontrada nesse abrigo"));

        StatusDoacao novo = switch (normalizar(statusTexto)) {
            case "APROVADO", "APROVADA", "CONFIRMADO", "CONFIRMADA" -> StatusDoacao.CONFIRMADA;
            case "RECUSADO", "RECUSADA", "CANCELADO", "CANCELADA" -> StatusDoacao.CANCELADA;
            case "ENTREGUE" -> StatusDoacao.ENTREGUE;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status inválido para doação (use APROVADA, RECUSADA ou ENTREGUE)");
        };

        if (DOACAO_FINAL.contains(d.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Doação já está " + d.getStatus().name() + " e não pode mais mudar");
        }

        // Entregue: soma na necessidade e fecha ela como ATENDIDA quando bater a meta
        if (novo == StatusDoacao.ENTREGUE) {
            if (d.getStatus() != StatusDoacao.CONFIRMADA) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Confirme a doação antes de registrar a entrega");
            }
            var n = necessidadeRepo.buscarComBloqueio(d.getNecessidade().getId()).orElseThrow();
            long recebida = (long) n.getQuantidadeRecebida() + d.getQuantidade();
            if (recebida > Integer.MAX_VALUE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Quantidade recebida excede o limite suportado");
            }
            n.setQuantidadeRecebida((int) recebida);
            if (n.getQuantidadeRecebida() >= n.getQuantidadeSolicitada()
                    && n.getStatus() == StatusNecessidade.ABERTA) {
                n.setStatus(StatusNecessidade.ATENDIDA);
            }
        }

        d.setStatus(novo);
        return SolicitacaoResponse.de(doacaoRepo.save(d));
    }

    private SolicitacaoResponse atualizarVoluntariado(Integer instituicaoId, Integer id, String statusTexto) {
        var v = voluntariadoRepo.findByIdAndInstituicaoId(id, instituicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Voluntariado não encontrado nesse abrigo"));

        StatusVoluntariado novo = switch (normalizar(statusTexto)) {
            case "APROVADO", "APROVADA", "CONFIRMADO", "CONFIRMADA" -> StatusVoluntariado.APROVADO;
            case "RECUSADO", "RECUSADA" -> StatusVoluntariado.RECUSADO;
            case "EM_ANDAMENTO" -> StatusVoluntariado.EM_ANDAMENTO;
            case "FINALIZADO", "FINALIZADA", "CONCLUIDO", "CONCLUIDA" -> StatusVoluntariado.FINALIZADO;
            case "CANCELADO", "CANCELADA" -> StatusVoluntariado.CANCELADO;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status inválido para voluntariado (use APROVADO, RECUSADO, EM_ANDAMENTO, FINALIZADO ou CANCELADO)");
        };

        if (VOLUNTARIADO_FINAL.contains(v.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Voluntariado já está " + v.getStatus().name() + " e não pode mais mudar");
        }

        if ((novo == StatusVoluntariado.EM_ANDAMENTO && v.getStatus() != StatusVoluntariado.APROVADO)
                || (novo == StatusVoluntariado.FINALIZADO && v.getStatus() != StatusVoluntariado.EM_ANDAMENTO)
                || (novo == StatusVoluntariado.APROVADO && v.getStatus() != StatusVoluntariado.PENDENTE)
                || (novo == StatusVoluntariado.RECUSADO && v.getStatus() != StatusVoluntariado.PENDENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transição de status inválida para voluntariado");
        }
        v.setStatus(novo);
        return SolicitacaoResponse.de(voluntariadoRepo.save(v));
    }

    private void exigirInstituicao(Integer id) {
        if (!instituicaoRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado");
        }
    }

    // "Doação " -> "DOACAO", "em andamento" -> "EM_ANDAMENTO"
    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "_");
    }
}
