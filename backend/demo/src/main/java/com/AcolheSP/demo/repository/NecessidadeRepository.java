package com.AcolheSP.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusNecessidade;

public interface NecessidadeRepository extends JpaRepository<Necessidade, Integer> {
    List<Necessidade> findByInstituicaoId(Integer instituicaoId);

    // UC02/UC04: necessidades atuais (abertas) de um abrigo
    List<Necessidade> findByInstituicaoIdAndStatus(Integer instituicaoId, StatusNecessidade status);

    // Garante que a necessidade pertence mesmo àquela instituição da URL
    Optional<Necessidade> findByIdAndInstituicaoId(Integer id, Integer instituicaoId);
}
