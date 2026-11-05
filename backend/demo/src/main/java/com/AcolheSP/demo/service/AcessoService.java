package com.AcolheSP.demo.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AcolheSP.demo.model.Conta;
import com.AcolheSP.demo.model.Voluntario;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;

/** Identidade vem da sessao autenticada, nunca de role/id enviados no JSON. */
@Service("acesso")
@Transactional(readOnly = true)
public class AcessoService {
    private final ContaRepository contas;
    private final InstituicaoRepository instituicoes;

    public AcessoService(ContaRepository contas, InstituicaoRepository instituicoes) {
        this.contas = contas;
        this.instituicoes = instituicoes;
    }

    private Conta conta(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        try {
            return contas.findById(Integer.valueOf(auth.getName()))
                    .filter(Conta::isAtivo).orElse(null);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public boolean admin(Authentication auth) {
        Conta conta = conta(auth);
        return conta != null && "admin".equals(conta.getRole());
    }

    public boolean abrigo(Authentication auth, Integer instituicaoId) {
        Conta conta = conta(auth);
        if (conta == null || !"abrigo".equals(conta.getRole())) return false;
        return instituicoes.findByConta_Id(conta.getId())
                .map(i -> i.getId().equals(instituicaoId)).orElse(false);
    }

    public boolean voluntario(Authentication auth, Integer voluntarioId) {
        Conta conta = conta(auth);
        return conta instanceof Voluntario && "usuario".equals(conta.getRole())
                && conta.getId().equals(voluntarioId);
    }

    public boolean ativo(Authentication auth) {
        return conta(auth) != null;
    }
}
