package com.AcolheSP.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.VoluntarioRequest;
import com.AcolheSP.demo.dto.VoluntarioResponse;
import com.AcolheSP.demo.model.Voluntario;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.VoluntarioRepository;

@Service
public class VoluntarioService {

    private final VoluntarioRepository voluntarioRepo;
    private final ContaRepository contaRepo;
    private final PasswordHasher passwordHasher;

    public VoluntarioService(VoluntarioRepository voluntarioRepo, ContaRepository contaRepo,
            PasswordHasher passwordHasher) {
        this.voluntarioRepo = voluntarioRepo;
        this.contaRepo = contaRepo;
        this.passwordHasher = passwordHasher;
    }

    // RF01 - Cadastrar voluntário (usuário)
    @Transactional
    public VoluntarioResponse cadastrar(VoluntarioRequest req) {
        exigir(req.email(), "email");
        exigir(req.nome(), "nome");
        if (contaRepo.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com esse email");
        }

        String senhaHash = passwordHasher.hashDaNovaSenha(req.senha());

        var v = new Voluntario();
        v.setEmail(req.email());
        v.setSenhaHash(senhaHash);
        v.setRole("usuario");
        v.setAtivo(true);
        v.setNome(req.nome());
        v.setTelefone(req.telefone());
        v.setDisponibilidade(req.disponibilidade());
        return VoluntarioResponse.de(voluntarioRepo.save(v));
    }

    @Transactional(readOnly = true)
    public VoluntarioResponse buscar(Integer id) {
        return VoluntarioResponse.de(voluntarioRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voluntário não encontrado")));
    }

    private static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório: " + campo);
        }
    }
}
