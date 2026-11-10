package com.AcolheSP.demo.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflitoDeDados(DataIntegrityViolationException ex) {
        // Inclui corridas de cadastro: uma restricao UNIQUE nao deve resultar em 500.
        // Nao expoe nomes internos de tabelas, valores de credenciais nem SQL.
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Os dados conflitam com um registro existente ou com uma operação em andamento");
    }
}
