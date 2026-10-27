package com.AcolheSP.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class Instituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", unique = true)
    private Conta conta;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 18, unique = true)
    private String cnpj;

    @Column(columnDefinition="text")
    private String descricao;

    @Column(length=20)
    private String telefone;

    @Column(name="email_contato")
    private String emailContato;

    @Column
    private Integer capacidade;

    @Column(name = "ocupacao_atual")
    private Integer ocupacaoAtual;

        @Enumerated(EnumType.STRING)
    @Column(name = "status_verificacao", nullable = false, length = 20)
    private StatusVerificacao statusVerificacao = StatusVerificacao.PENDENTE;

    @OneToOne(
        mappedBy = "instituicao",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Localizacao localizacao;
}
