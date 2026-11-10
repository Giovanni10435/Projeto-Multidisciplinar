package com.AcolheSP.demo.model;

import java.time.LocalTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "horario_voluntariado")
@Getter
@Setter
@NoArgsConstructor
public class HorarioVoluntariado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;
    @Column(nullable = false, length = 20)
    private String dia;
    @Column(nullable = false, length = 150)
    private String funcao;
    @Column(nullable = false)
    private LocalTime inicio;
    @Column(nullable = false)
    private LocalTime fim;
    @Column(nullable = false)
    private boolean ativo = true;
}
