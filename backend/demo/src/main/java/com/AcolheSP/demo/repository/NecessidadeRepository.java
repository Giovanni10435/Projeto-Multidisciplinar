package com.AcolheSP.demo.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusNecessidade;

public interface NecessidadeRepository extends JpaRepository<Necessidade, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Necessidade n where n.id = :id")
    Optional<Necessidade> buscarComBloqueio(@Param("id") Integer id);

    List<Necessidade> findByInstituicaoId(Integer instituicaoId);

    // UC02/UC04: necessidades atuais (abertas) de um abrigo
    List<Necessidade> findByInstituicaoIdAndStatus(Integer instituicaoId, StatusNecessidade status);

    // Garante que a necessidade pertence mesmo àquela instituição da URL
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Necessidade> findByIdAndInstituicaoId(Integer id, Integer instituicaoId);

    // UC03: quantas necessidades abertas o abrigo tem (mostrado no mapa)
    long countByInstituicaoIdAndStatus(Integer instituicaoId, StatusNecessidade status);
}
