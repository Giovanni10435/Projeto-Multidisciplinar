package com.AcolheSP.demo.repository;

import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.StatusNecessidade;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Integer> {
    boolean existsByCnpj(String cnpj);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Instituicao i where i.id = :id")
    Optional<Instituicao> buscarComBloqueio(@Param("id") Integer id);

    // UC01: busca por nome (parte do nome, sem diferenciar maiúscula/minúscula)
    List<Instituicao> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    // Apenas para chamar e encontrar por ID atrelado ao abrigo, caso tenha
    Optional<Instituicao> findByConta_Id(Integer contaId);

    // UC01: busca por nome + tipo de necessidade (abrigos que têm necessidade ABERTA com esse texto)
    @Query("""
            select i from Instituicao i
            where lower(i.nome) like lower(concat('%', :nome, '%'))
              and exists (
                    select 1 from Necessidade n
                    where n.instituicao = i
                      and n.status = :status
                      and lower(n.descricao) like lower(concat('%', :necessidade, '%')))
            order by i.nome
            """)
    List<Instituicao> buscarPorNomeENecessidade(
            @Param("nome") String nome,
            @Param("necessidade") String necessidade,
            @Param("status") StatusNecessidade status);
}
