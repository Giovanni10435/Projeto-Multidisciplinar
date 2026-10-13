package com.AcolheSP.demo.dto;

import com.AcolheSP.demo.model.Voluntario;

public record VoluntarioResponse(
        Integer id,
        String email,
        String nome,
        String telefone,
        String disponibilidade) {

    public static VoluntarioResponse de(Voluntario v) {
        return new VoluntarioResponse(v.getId(), v.getEmail(), v.getNome(), v.getTelefone(), v.getDisponibilidade());
    }
}
