package com.AcolheSP.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Mensagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String texto;

    public Mensagem() {}
    public Mensagem(String texto) { this.texto = texto; }
    public String getTexto() { return texto; }
}
