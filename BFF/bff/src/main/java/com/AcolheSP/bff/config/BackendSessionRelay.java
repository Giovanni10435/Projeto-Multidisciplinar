package com.AcolheSP.bff.config;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/** Repassa a sessao de cada requisicao, sem compartilhar cookies entre usuarios. */
@Component
public class BackendSessionRelay implements WebFilter {
    private static final Object CONTEXT_KEY = new Object();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).contextWrite(context -> context.put(CONTEXT_KEY, exchange));
    }

    public static ExchangeFilterFunction forwardingFilter() {
        return (request, next) -> Mono.deferContextual(context -> {
            if (!context.hasKey(CONTEXT_KEY)) return next.exchange(request);
            ServerWebExchange exchange = context.get(CONTEXT_KEY);
            var forwarded = ClientRequest.from(request);
            var cookies = exchange.getRequest().getCookies().get("JSESSIONID");
            if (cookies != null) cookies.forEach(cookie -> forwarded.cookie("JSESSIONID", cookie.getValue()));
            String csrf = exchange.getRequest().getHeaders().getFirst("X-CSRF-TOKEN");
            if (csrf != null) forwarded.header("X-CSRF-TOKEN", csrf);
            return next.exchange(forwarded.build()).doOnNext(response -> {
                var setCookies = response.headers().header(HttpHeaders.SET_COOKIE);
                if (!setCookies.isEmpty()) {
                    exchange.getResponse().getHeaders().addAll(HttpHeaders.SET_COOKIE, setCookies);
                }
            });
        });
    }
}
