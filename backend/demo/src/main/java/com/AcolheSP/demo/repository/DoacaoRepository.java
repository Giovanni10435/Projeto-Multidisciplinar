package com.AcolheSP.demo.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import com.AcolheSP.demo.model.StatusDoacao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.Doacao;

public interface DoacaoRepository extends JpaRepository<Doacao, Integer> {

    @Query("""
            select coalesce(sum(d.quantidade), 0) from Doacao d
            where d.necessidade.id = :id and d.status in :status
            """)
    long quantidadeComprometida(@Param("id") Integer id,
            @Param("status") Collection<StatusDoacao> status);

    boolean existsByNecessidadeId(Integer necessidadeId);

    // UC11: doações destinadas a necessidades de uma instituição
    List<Doacao> findByNecessidadeInstituicaoId(Integer instituicaoId);

    // Garante que a doação é mesmo de uma necessidade daquela instituição
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Doacao> findByIdAndNecessidadeInstituicaoId(Integer id, Integer instituicaoId);
}
