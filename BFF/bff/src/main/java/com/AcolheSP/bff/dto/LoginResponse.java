package com.AcolheSP.bff.dto;

public record LoginResponse(
    Integer id,
    String email,
    String role,
    String nome
) {
}