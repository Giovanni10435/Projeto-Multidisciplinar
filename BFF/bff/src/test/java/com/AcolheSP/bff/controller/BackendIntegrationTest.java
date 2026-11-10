package com.AcolheSP.bff.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.AcolheSP.bff.config.BackendSessionRelay;
import reactor.core.publisher.Mono;

@SpringBootTest
@Import(BackendIntegrationTest.BackendStubConfig.class)
class BackendIntegrationTest {
    @Autowired ApplicationContext context;
    @Autowired BackendStub stub;
    private WebTestClient client;
    private static final String LOGIN = "{\"id\":1,\"email\":\"ana@exemplo.com\",\"nome\":\"Ana\",\"role\":\"usuario\",\"instituicaoId\":null}";

    @TestConfiguration(proxyBeanMethods = false)
    static class BackendStubConfig {
        @Bean BackendStub backendStub() { return new BackendStub(); }
        @Bean @Primary WebClient testBackendWebClient(BackendStub stub) {
            return WebClient.builder().baseUrl("http://backend.test")
                    .filter(BackendSessionRelay.forwardingFilter()).exchangeFunction(stub).build();
        }
    }

    record Captured(ClientRequest request, String body) { }

    static class BackendStub implements ExchangeFunction {
        final Queue<Captured> requests = new ConcurrentLinkedQueue<>();
        BiFunction<ClientRequest, String, ClientResponse> handler;
        public Mono<ClientResponse> exchange(ClientRequest request) {
            var wire = new MockClientHttpRequest(request.method(), request.url());
            wire.getHeaders().addAll(request.headers());
            var bodyContext = new BodyInserter.Context() {
                public List<HttpMessageWriter<?>> messageWriters() { return ExchangeStrategies.withDefaults().messageWriters(); }
                public Optional<ServerHttpRequest> serverRequest() { return Optional.empty(); }
                public Map<String, Object> hints() { return Map.of(); }
            };
            return request.body().insert(wire, bodyContext)
                    .then(Mono.defer(() -> wire.getBodyAsString().defaultIfEmpty("")))
                    .map(body -> {
                        requests.add(new Captured(request, body));
                        return handler.apply(request, body);
                    });
        }
    }

    private static ClientResponse json(HttpStatus status, String body) {
        return ClientResponse.create(status).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body).build();
    }

    @BeforeEach
    void preparar() {
        stub.requests.clear();
        stub.handler = (request, body) -> json(HttpStatus.OK, "[]");
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void csrfEncaminhaTokenECookieHttpOnlyAoNavegador() {
        stub.handler = (request, body) -> ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.SET_COOKIE, "JSESSIONID=inicial; Path=/; HttpOnly; SameSite=Lax")
                .body("{\"headerName\":\"X-CSRF-TOKEN\",\"token\":\"token-inicial\"}").build();
        client.get().uri("/api/auth/csrf").exchange().expectStatus().isOk()
                .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, ".*JSESSIONID=inicial.*HttpOnly.*")
                .expectBody().jsonPath("$.token").isEqualTo("token-inicial");
    }

    @Test
    void loginEncaminhaSessaoECsrfERetornaCookieRenovado() {
        stub.handler = (request, body) -> ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.SET_COOKIE, "JSESSIONID=renovado; Path=/; HttpOnly")
                .body(LOGIN).build();
        client.post().uri("/api/auth/login").cookie("JSESSIONID", "inicial")
                .header("X-CSRF-TOKEN", "token-inicial").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "ana@exemplo.com", "senha", "senha123"))
                .exchange().expectStatus().isOk().expectHeader().valueMatches(HttpHeaders.SET_COOKIE, ".*renovado.*")
                .expectBody().jsonPath("$.id").isEqualTo(1);
        var request = stub.requests.remove().request();
        assertEquals("inicial", request.cookies().getFirst("JSESSIONID"));
        assertEquals("token-inicial", request.headers().getFirst("X-CSRF-TOKEN"));
    }

    @Test
    void contaAtualRecebeCookieDaRequisicao() {
        stub.handler = (request, body) -> json(HttpStatus.OK, LOGIN);
        client.get().uri("/api/auth/me").cookie("JSESSIONID", "usuario-a").exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.nome").isEqualTo("Ana");
        assertEquals("usuario-a", stub.requests.remove().request().cookies().getFirst("JSESSIONID"));
    }

    @Test
    void logoutPreserva204EExpiracaoDoCookie() {
        stub.handler = (request, body) -> ClientResponse.create(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, "JSESSIONID=; Path=/; Max-Age=0; HttpOnly").build();
        client.post().uri("/api/auth/logout").cookie("JSESSIONID", "usuario-a")
                .header("X-CSRF-TOKEN", "token").exchange().expectStatus().isNoContent()
                .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, ".*Max-Age=0.*");
        assertEquals("usuario-a", stub.requests.remove().request().cookies().getFirst("JSESSIONID"));
    }

    @Test
    void horariosPassamPelasNovasRotasPreservandoStatus() {
        String base = "/api/instituicoes/7/horarios-voluntariado";
        String body = "{\"dia\":\"Sábado\",\"funcao\":\"Cozinha\",\"inicio\":\"08:00\",\"fim\":\"12:00\"}";
        stub.handler = (request, data) -> {
            if (request.method().name().equals("POST")) return ClientResponse.create(HttpStatus.CREATED)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.LOCATION, base + "/3").body("{\"id\":3}").build();
            if (request.method().name().equals("DELETE")) return ClientResponse.create(HttpStatus.NO_CONTENT).build();
            return json(HttpStatus.OK, "[]");
        };
        client.get().uri(base).exchange().expectStatus().isOk();
        client.post().uri(base).contentType(MediaType.APPLICATION_JSON).bodyValue(body).exchange()
                .expectStatus().isCreated().expectHeader().valueEquals(HttpHeaders.LOCATION, base + "/3");
        client.put().uri(base + "/3").contentType(MediaType.APPLICATION_JSON).bodyValue(body).exchange().expectStatus().isOk();
        client.delete().uri(base + "/3").exchange().expectStatus().isNoContent();
        assertEquals(4, stub.requests.size());
    }

    @Test
    void candidaturaUsaInstituicaoDaUrlEmVezDoCampoDoCliente() {
        stub.handler = (request, body) -> json(HttpStatus.CREATED, "{\"id\":3}");
        client.post().uri("/api/instituicoes/7/voluntariados").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("voluntarioId", 1, "horarioId", 3, "instituicao_id", 999))
                .exchange().expectStatus().is2xxSuccessful();
        var forwarded = stub.requests.remove();
        assertEquals("/api/instituicoes/7/voluntariados", forwarded.request().url().getPath());
        assertTrue(forwarded.body().contains("\"instituicao_id\":7"));
    }

    @Test
    void cadastroNaoModificaCaracteresDaSenha() {
        stub.handler = (request, body) -> json(HttpStatus.CREATED, LOGIN);
        client.post().uri("/api/voluntarios").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("nome", "Ana", "email", "ana@exemplo.com", "senha", "Ab'c;--12"))
                .exchange().expectStatus().isCreated();
        assertTrue(stub.requests.remove().body().contains("Ab'c;--12"));
    }

    @Test
    void conflitoDoBackendContinua409ComAMensagemOriginal() {
        stub.handler = (request, body) -> json(HttpStatus.CONFLICT, "{\"detail\":\"Saldo indisponível\"}");
        client.post().uri("/api/doacoes").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("voluntarioId", 1, "necessidadeId", 2, "quantidade", 5))
                .exchange().expectStatus().isEqualTo(409).expectBody().jsonPath("$.detail").isEqualTo("Saldo indisponível");
    }

    @Test
    void requisicoesConcorrentesNaoCompartilhamCookiesOuCsrf() {
        var relay = new BackendSessionRelay();
        var upstream = WebClient.builder().baseUrl("http://backend.test")
                .filter(BackendSessionRelay.forwardingFilter())
                .exchangeFunction(request -> {
                    String session = request.cookies().getFirst("JSESSIONID");
                    assertEquals("csrf-" + session, request.headers().getFirst("X-CSRF-TOKEN"));
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                            .header(HttpHeaders.SET_COOKIE, "JSESSIONID=" + session + "-renovado; Path=/; HttpOnly")
                            .body(session).build()).delayElement(Duration.ofMillis(10));
                }).build();
        var a = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/me")
                .cookie(new HttpCookie("JSESSIONID", "a")).header("X-CSRF-TOKEN", "csrf-a"));
        var b = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/me")
                .cookie(new HttpCookie("JSESSIONID", "b")).header("X-CSRF-TOKEN", "csrf-b"));
        Mono.when(
                relay.filter(a, exchange -> upstream.get().uri("/api/auth/me").retrieve().bodyToMono(String.class)
                        .doOnNext(body -> assertEquals("a", body)).then()),
                relay.filter(b, exchange -> upstream.get().uri("/api/auth/me").retrieve().bodyToMono(String.class)
                        .doOnNext(body -> assertEquals("b", body)).then())).block(Duration.ofSeconds(5));
        assertTrue(a.getResponse().getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("a-renovado"));
        assertTrue(b.getResponse().getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("b-renovado"));
    }
}
