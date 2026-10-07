package com.AcolheSP.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Localizacao;
import com.AcolheSP.demo.model.StatusVerificacao;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;

@Service
public class AbrigoService {

    private final InstituicaoRepository instituicaoRepo;
    private final ContaRepository contaRepo;

    public AbrigoService(InstituicaoRepository instituicaoRepo, ContaRepository contaRepo) {
        this.instituicaoRepo = instituicaoRepo;
        this.contaRepo = contaRepo;
    }

    // UC07 - Cadastrar abrigo
    @Transactional
    public AbrigoResponse cadastrarAbrigo(AbrigoRequest req) {
        // Sem login por enquanto: se não vier "email", usa o emailContato como login da conta
        String email = vazioParaNull(req.email()) != null ? req.email() : req.emailContato();

        exigir(email, "email");
        exigir(req.nome(), "nome");
        exigir(req.endereco(), "endereco");
        if (req.latitude() == null || req.longitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "latitude e longitude são obrigatórias");
        }
        if (contaRepo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com esse email");
        }
        if (vazioParaNull(req.cnpj()) != null && instituicaoRepo.existsByCnpj(req.cnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um abrigo com esse CNPJ");
        }

        var inst = new Instituicao();
        inst.setEmail(email);
        inst.setRole("abrigo");
        inst.setAtivo(true);
        inst.setNome(req.nome());
        inst.setCnpj(vazioParaNull(req.cnpj()));
        inst.setDescricao(req.descricao());
        inst.setTelefone(req.telefone());
        inst.setEmailContato(req.emailContato());
        inst.setStatusVerificacao(StatusVerificacao.PENDENTE);

        var loc = new Localizacao();
        loc.setEndereco(req.endereco());
        loc.setCep(vazioParaNull(req.cep()));
        loc.setLatitude(req.latitude());
        loc.setLongitude(req.longitude());
        loc.setInstituicao(inst);
        inst.setLocalizacao(loc);

        return AbrigoResponse.de(instituicaoRepo.save(inst));
    }

    @Transactional(readOnly = true)
    public AbrigoResponse buscarAbrigo(Integer id) {
        return instituicaoRepo.findById(id)
                .map(AbrigoResponse::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));
    }

    private static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: " + campo);
        }
    }

    private static String vazioParaNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
