package com.AcolheSP.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.LoginRequest;
import com.AcolheSP.demo.dto.LoginResponse;
import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;

@Service
public class AuthService {

    private final ContaRepository contaRepo;
    private final PasswordHasher passwordHasher;
    private final InstituicaoRepository instituicaoRepo;

    public AuthService(ContaRepository contaRepo, PasswordHasher passwordHasher, InstituicaoRepository instituicaoRepo) {
        this.contaRepo = contaRepo;
        this.passwordHasher = passwordHasher;
        this.instituicaoRepo = instituicaoRepo;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        if (req == null || req.email() == null || req.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: email");
        }
        if (req.senha() == null || req.senha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: senha");
        }

        // Mesma resposta pra email inexistente, conta sem senha e senha errada
        Conta conta = contaRepo.findByEmail(req.email().trim())
                .orElseThrow(AuthService::credenciaisInvalidas);

        if (!passwordHasher.confere(req.senha(), conta.getSenhaHash())) {
            throw credenciaisInvalidas();
        }
        if (!conta.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta inativa");
        }

        
        var instituicao = instituicaoRepo.findByConta_Id(conta.getId()).orElse(null);

        return LoginResponse.de(conta, instituicao);
    }

    private static ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos");
    }
}