package com.AcolheSP.demo.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.StatusVoluntariado;
import com.AcolheSP.demo.model.Voluntariado;

public interface VoluntariadoRepository extends JpaRepository<Voluntariado, Integer> {

    List<Voluntariado> findByInstituicaoId(Integer instituicaoId);

    Optional<Voluntariado> findByIdAndInstituicaoId(Integer id, Integer instituicaoId);

    // Evita o mesmo voluntário se candidatar duas vezes enquanto a candidatura anterior está ativa
    boolean existsByVoluntarioIdAndInstituicaoIdAndStatusIn(
            Integer voluntarioId, Integer instituicaoId, Collection<StatusVoluntariado> status);
}
