package com.AcolheSP.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// role = usuario. Mesma estratégia JOINED da Instituicao (tabela voluntario.id = conta.id)
@Entity
@Table(name = "voluntario")
@Getter
@Setter
@NoArgsConstructor
public class Voluntario extends Conta {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(length = 255)
    private String disponibilidade;
}
