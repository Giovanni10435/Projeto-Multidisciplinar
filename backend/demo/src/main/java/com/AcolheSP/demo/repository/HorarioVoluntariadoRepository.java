package com.AcolheSP.demo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.AcolheSP.demo.model.HorarioVoluntariado;

public interface HorarioVoluntariadoRepository extends JpaRepository<HorarioVoluntariado, Integer> {
    List<HorarioVoluntariado> findByInstituicaoIdAndAtivoTrue(Integer instituicaoId);
    Optional<HorarioVoluntariado> findByIdAndInstituicaoId(Integer id, Integer instituicaoId);
}
