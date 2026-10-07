package com.AcolheSP.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instituicao")
@Getter
@Setter
@NoArgsConstructor
public class Instituicao extends Conta {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 18, unique = true)
    private String cnpj;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(length = 20)
    private String telefone;

    @Column(name = "email_contato")
    private String emailContato;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_verificacao", nullable = false, length = 20)
    private StatusVerificacao statusVerificacao = StatusVerificacao.PENDENTE;

    @OneToOne(mappedBy = "instituicao", cascade = CascadeType.ALL)
    private Localizacao localizacao;
}
