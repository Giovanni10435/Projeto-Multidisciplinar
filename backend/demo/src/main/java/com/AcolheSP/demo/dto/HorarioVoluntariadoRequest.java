package com.AcolheSP.demo.dto;

import java.time.LocalTime;

public record HorarioVoluntariadoRequest(String dia, String funcao, LocalTime inicio, LocalTime fim) {
}
