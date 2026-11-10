package com.AcolheSP.demo.dto;

import java.time.LocalTime;
import com.AcolheSP.demo.model.HorarioVoluntariado;

public record HorarioVoluntariadoResponse(Integer id, Integer instituicaoId, String dia,
        String funcao, LocalTime inicio, LocalTime fim, boolean ativo) {
    public static HorarioVoluntariadoResponse de(HorarioVoluntariado h) {
        return new HorarioVoluntariadoResponse(h.getId(), h.getInstituicao().getId(), h.getDia(),
                h.getFuncao(), h.getInicio(), h.getFim(), h.isAtivo());
    }
}
