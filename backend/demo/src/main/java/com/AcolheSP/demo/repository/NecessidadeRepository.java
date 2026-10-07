package com.AcolheSP.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.Necessidade;

public interface NecessidadeRepository extends JpaRepository<Necessidade, Integer> {
    List<Necessidade> findByInstituicaoId(Integer instituicaoId);

    // Garante que a necessidade pertence mesmo àquela instituição da URL
    Optional<Necessidade> findByIdAndInstituicaoId(Integer id, Integer instituicaoId);
}
