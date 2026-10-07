package com.AcolheSP.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.Instituicao;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Integer> {
    boolean existsByCnpj(String cnpj);
}
