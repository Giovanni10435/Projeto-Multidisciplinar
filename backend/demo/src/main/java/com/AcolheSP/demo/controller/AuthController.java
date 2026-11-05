package com.AcolheSP.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AcolheSP.demo.dto.LoginRequest;
import com.AcolheSP.demo.dto.LoginResponse;
import com.AcolheSP.demo.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest dados,
            HttpServletRequest request, HttpServletResponse response) {
        LoginResponse conta = authService.login(dados);
        if (request.getSession(false) != null) request.changeSessionId();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                conta.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + conta.role())));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        new CsrfAuthenticationStrategy(new HttpSessionCsrfTokenRepository())
                .onAuthentication(authentication, request, response);
        return conta;
    }
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/me")
    @PreAuthorize("@acesso.ativo(authentication)")
    public LoginResponse me() {
        Integer id = Integer.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        return authService.buscarConta(id);
    }
}
