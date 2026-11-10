package com.AcolheSP.demo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AcolheSP.demo.model.Conta;

public interface ContaRepository extends JpaRepository<Conta, Integer> {
    boolean existsByEmail(String email);

    Optional<Conta> findByEmail(String email);

    Optional<Conta> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Conta> findByKeycloakId(UUID keycloakId);
}
