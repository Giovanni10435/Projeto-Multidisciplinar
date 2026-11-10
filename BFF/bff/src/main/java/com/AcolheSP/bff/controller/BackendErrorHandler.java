package com.AcolheSP.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/** Preserva 400/401/403/404/409 recebidos do backend, em vez de converte-los em 500. */
@RestControllerAdvice
public class BackendErrorHandler {
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<byte[]> backendError(WebClientResponseException error) {
        var response = ResponseEntity.status(error.getStatusCode());
        if (error.getHeaders().getContentType() != null) response.contentType(error.getHeaders().getContentType());
        return response.body(error.getResponseBodyAsByteArray());
    }
}
