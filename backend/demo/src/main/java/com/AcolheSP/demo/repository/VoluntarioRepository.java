package com.AcolheSP.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.Voluntario;

public interface VoluntarioRepository extends JpaRepository<Voluntario, Integer> {
}
