package com.AcolheSP.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.Doacao;

public interface DoacaoRepository extends JpaRepository<Doacao, Integer> {

    // UC11: doações destinadas a necessidades de uma instituição
    List<Doacao> findByNecessidadeInstituicaoId(Integer instituicaoId);

    // Garante que a doação é mesmo de uma necessidade daquela instituição
    Optional<Doacao> findByIdAndNecessidadeInstituicaoId(Integer id, Integer instituicaoId);
}
