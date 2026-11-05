package com.AcolheSP.demo.service;

import com.AcolheSP.demo.model.StatusDoacao;
import com.AcolheSP.demo.repository.DoacaoRepository;
import java.util.Locale;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.NecessidadeRequest;
import com.AcolheSP.demo.dto.NecessidadeResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.NecessidadeRepository;

@Service
public class NecessidadeService {

    private final NecessidadeRepository necessidadeRepo;
    private final InstituicaoRepository instituicaoRepo;
    private final DoacaoRepository doacaoRepo;

    public NecessidadeService(NecessidadeRepository necessidadeRepo, InstituicaoRepository instituicaoRepo,
            DoacaoRepository doacaoRepo) {
        this.necessidadeRepo = necessidadeRepo;
        this.instituicaoRepo = instituicaoRepo;
        this.doacaoRepo = doacaoRepo;
    }

    // UC09 - Cadastrar necessidade
    @Transactional
    public NecessidadeResponse cadastrarNecessidade(Integer instituicaoId, NecessidadeRequest req) {
        Instituicao inst = buscarInstituicao(instituicaoId);

        if (req == null || req.descricao() == null || req.descricao().isBlank() || req.descricao().trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: descricao");
        }
        if (req.quantidadeSolicitada() == null || req.quantidadeSolicitada() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantidadeSolicitada deve ser maior que zero");
        }

        var n = new Necessidade();
        n.setInstituicao(inst);
        n.setDescricao(req.descricao().trim());
        n.setQuantidadeSolicitada(req.quantidadeSolicitada());
        n.setQuantidadeRecebida(validarRecebida(req.quantidadeRecebida(), 0));
        n.setStatus(converterStatus(req.status(), StatusNecessidade.ABERTA));

        validarSaldo(n);
        return NecessidadeResponse.de(necessidadeRepo.save(n));
    }

    // UC10 - Atualizar necessidade (só muda o que vier preenchido)
    @Transactional
    public NecessidadeResponse atualizarNecessidade(Integer instituicaoId, Integer necessidadeId, NecessidadeRequest req) {
        Necessidade n = buscarNecessidade(instituicaoId, necessidadeId);
        if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corpo obrigatório");
        if (req.descricao() != null) {
            if (req.descricao().isBlank() || req.descricao().trim().length() > 255) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "descricao deve ter entre 1 e 255 caracteres");
            }
            n.setDescricao(req.descricao().trim());
        }
        if (req.quantidadeSolicitada() != null) {
            if (req.quantidadeSolicitada() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantidadeSolicitada deve ser maior que zero");
            }
            n.setQuantidadeSolicitada(req.quantidadeSolicitada());
        }
        if (req.quantidadeRecebida() != null) {
            n.setQuantidadeRecebida(validarRecebida(req.quantidadeRecebida(), 0));
        }
        n.setStatus(converterStatus(req.status(), n.getStatus()));

        validarSaldo(n);
        return NecessidadeResponse.de(necessidadeRepo.save(n));
    }

    // Remover necessidade
    @Transactional
    public void removerNecessidade(Integer instituicaoId, Integer necessidadeId) {
        Necessidade n = buscarNecessidade(instituicaoId, necessidadeId);
        if (doacaoRepo.existsByNecessidadeId(necessidadeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Necessidade com doações não pode ser removida; encerre a necessidade");
        }
        necessidadeRepo.delete(n);
    }

    // Extra (para teste): lista as necessidades de um abrigo
    @Transactional(readOnly = true)
    public List<NecessidadeResponse> listarNecessidades(Integer instituicaoId) {
        buscarInstituicao(instituicaoId);
        return necessidadeRepo.findByInstituicaoId(instituicaoId).stream()
                .map(NecessidadeResponse::de)
                .toList();
    }

    private Instituicao buscarInstituicao(Integer id) {
        return instituicaoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));
    }

    private Necessidade buscarNecessidade(Integer instituicaoId, Integer necessidadeId) {
        return necessidadeRepo.findByIdAndInstituicaoId(necessidadeId, instituicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Necessidade não encontrada nesse abrigo"));
    }

    private void validarSaldo(Necessidade n) {
        long comprometida = n.getId() == null ? 0 : doacaoRepo.quantidadeComprometida(n.getId(),
                List.of(StatusDoacao.PENDENTE, StatusDoacao.CONFIRMADA));
        if ((long) n.getQuantidadeRecebida() + comprometida > n.getQuantidadeSolicitada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "quantidadeSolicitada não pode ser menor que o total recebido e comprometido");
        }
        if (n.getStatus() == StatusNecessidade.ABERTA && n.getQuantidadeRecebida().equals(n.getQuantidadeSolicitada())) {
            n.setStatus(StatusNecessidade.ATENDIDA);
        }
    }

    private static Integer validarRecebida(Integer valor, int padrao) {
        if (valor == null) {
            return padrao;
        }
        if (valor < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantidadeRecebida não pode ser negativa");
        }
        return valor;
    }

    private static StatusNecessidade converterStatus(String texto, StatusNecessidade padrao) {
        if (texto == null || texto.isBlank()) {
            return padrao;
        }
        try {
            return StatusNecessidade.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status inválido (use ABERTA, ATENDIDA ou ENCERRADA)");
        }
    }
}
