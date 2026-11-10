package com.AcolheSP.demo.service;

import java.util.Locale;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoDetalheResponse;
import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.dto.NecessidadeResponse;
import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Localizacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.model.StatusVerificacao;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.NecessidadeRepository;

@Service
public class AbrigoService {

    private final InstituicaoRepository instituicaoRepo;
    private final ContaRepository contaRepo;
    private final NecessidadeRepository necessidadeRepo;
    private final PasswordHasher passwordHasher;

    public AbrigoService(InstituicaoRepository instituicaoRepo, ContaRepository contaRepo,
            NecessidadeRepository necessidadeRepo, PasswordHasher passwordHasher) {
        this.instituicaoRepo = instituicaoRepo;
        this.contaRepo = contaRepo;
        this.necessidadeRepo = necessidadeRepo;
        this.passwordHasher = passwordHasher;
    }

    // UC07 - Cadastrar abrigo
    @Transactional
    public AbrigoResponse cadastrarAbrigo(AbrigoRequest req) {
        exigir(req.nome(), "nome");
        exigir(req.endereco(), "endereco");

        if (req.latitude() == null || req.longitude() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "latitude e longitude são obrigatórias"
            );
        }

        String cnpj = vazioParaNull(req.cnpj());

        if (cnpj != null && instituicaoRepo.existsByCnpj(cnpj)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um abrigo com esse CNPJ"
            );
        }

        String email = vazioParaNull(req.email());
        boolean temEmail = email != null;
        boolean temSenha = vazioParaNull(req.senha()) != null;

        if (!temEmail && temSenha) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Para definir uma senha, informe o email da conta"
            );
        }

        if (temEmail && contaRepo.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma conta com esse email"
            );
        }

        var inst = new Instituicao();
        inst.setNome(req.nome());
        inst.setCnpj(cnpj);
        inst.setDescricao(req.descricao());
        inst.setTelefone(req.telefone());
        inst.setEmailContato(req.emailContato());
        inst.setStatusVerificacao(StatusVerificacao.PENDENTE);

        // Cria e vincula uma conta somente quando há credenciais.
        if (temEmail) {
            var conta = new Conta();
            conta.setEmail(email.trim().toLowerCase(Locale.ROOT));
            conta.setSenhaHash(
                    passwordHasher.hashDaNovaSenha(req.senha())
            );
            conta.setRole("abrigo");
            conta.setAtivo(true);

            inst.setConta(contaRepo.save(conta));
        }

        var loc = new Localizacao();
        loc.setEndereco(req.endereco());
        loc.setCep(vazioParaNull(req.cep()));
        loc.setLatitude(req.latitude());
        loc.setLongitude(req.longitude());
        loc.setInstituicao(inst);

        inst.setLocalizacao(loc);

        return AbrigoResponse.de(instituicaoRepo.save(inst));
    }

    // UC01 - Consultar locais de assistência (filtros opcionais: nome e tipo de necessidade)
    @Transactional(readOnly = true)
    public List<AbrigoResponse> listarAbrigos(String nome, String necessidade) {
        String nomeFiltro = nome == null ? "" : nome.trim();
        String necessidadeFiltro = necessidade == null ? "" : necessidade.trim();

        List<Instituicao> encontrados = necessidadeFiltro.isEmpty()
                ? instituicaoRepo.findByNomeContainingIgnoreCaseOrderByNome(nomeFiltro)
                : instituicaoRepo.buscarPorNomeENecessidade(nomeFiltro, necessidadeFiltro, StatusNecessidade.ABERTA);

        return encontrados.stream().map(AbrigoResponse::de).toList();
    }

    // UC02 - Visualizar informações do abrigo (+ necessidades abertas, UC04)
    @Transactional(readOnly = true)
    public AbrigoDetalheResponse detalharAbrigo(Integer id) {
        Instituicao inst = instituicaoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));

        var necessidades = necessidadeRepo.findByInstituicaoIdAndStatus(id, StatusNecessidade.ABERTA).stream()
                .map(NecessidadeResponse::de)
                .toList();

        return new AbrigoDetalheResponse(AbrigoResponse.de(inst), necessidades);
    }

    // UC08 - Atualizar informações do abrigo (só muda o que vier preenchido)
    @Transactional
    public AbrigoResponse atualizarAbrigo(Integer id, AbrigoRequest req) {
        Instituicao inst = instituicaoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Abrigo não encontrado"));

        if (vazioParaNull(req.nome()) != null) {
            inst.setNome(req.nome());
        }
        if (vazioParaNull(req.descricao()) != null) {
            inst.setDescricao(req.descricao());
        }
        if (vazioParaNull(req.telefone()) != null) {
            inst.setTelefone(req.telefone());
        }
        if (vazioParaNull(req.emailContato()) != null) {
            inst.setEmailContato(req.emailContato());
        }

        String cnpj = vazioParaNull(req.cnpj());
        if (cnpj != null && !cnpj.equals(inst.getCnpj())) {
            if (instituicaoRepo.existsByCnpj(cnpj)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um abrigo com esse CNPJ");
            }
            inst.setCnpj(cnpj);
        }

        Localizacao loc = inst.getLocalizacao();
        if (vazioParaNull(req.endereco()) != null) {
            loc.setEndereco(req.endereco());
        }
        if (vazioParaNull(req.cep()) != null) {
            loc.setCep(req.cep());
        }
        if (req.latitude() != null) {
            loc.setLatitude(req.latitude());
        }
        if (req.longitude() != null) {
            loc.setLongitude(req.longitude());
        }

        return AbrigoResponse.de(instituicaoRepo.save(inst));
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
