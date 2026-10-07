package com.AcolheSP.demo.service;

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

    public NecessidadeService(NecessidadeRepository necessidadeRepo, InstituicaoRepository instituicaoRepo) {
        this.necessidadeRepo = necessidadeRepo;
        this.instituicaoRepo = instituicaoRepo;
    }

    // UC09 - Cadastrar necessidade
    @Transactional
    public NecessidadeResponse cadastrarNecessidade(Integer instituicaoId, NecessidadeRequest req) {
        Instituicao inst = buscarInstituicao(instituicaoId);

        if (req.descricao() == null || req.descricao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: descricao");
        }
        if (req.quantidadeSolicitada() == null || req.quantidadeSolicitada() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantidadeSolicitada deve ser maior que zero");
        }

        var n = new Necessidade();
        n.setInstituicao(inst);
        n.setDescricao(req.descricao());
        n.setQuantidadeSolicitada(req.quantidadeSolicitada());
        n.setQuantidadeRecebida(validarRecebida(req.quantidadeRecebida(), 0));
        n.setStatus(converterStatus(req.status(), StatusNecessidade.ABERTA));

        return NecessidadeResponse.de(necessidadeRepo.save(n));
    }

    // UC10 - Atualizar necessidade (só muda o que vier preenchido)
    @Transactional
    public NecessidadeResponse atualizarNecessidade(Integer instituicaoId, Integer necessidadeId, NecessidadeRequest req) {
        Necessidade n = buscarNecessidade(instituicaoId, necessidadeId);

        if (req.descricao() != null && !req.descricao().isBlank()) {
            n.setDescricao(req.descricao());
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

        return NecessidadeResponse.de(necessidadeRepo.save(n));
    }

    // Remover necessidade
    @Transactional
    public void removerNecessidade(Integer instituicaoId, Integer necessidadeId) {
        necessidadeRepo.delete(buscarNecessidade(instituicaoId, necessidadeId));
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
            return StatusNecessidade.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status inválido (use ABERTA, ATENDIDA ou ENCERRADA)");
        }
    }
}
