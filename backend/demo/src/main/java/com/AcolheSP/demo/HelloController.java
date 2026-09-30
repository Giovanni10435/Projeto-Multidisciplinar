package com.AcolheSP.demo;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    private final MensagemRepository repo;

    public HelloController(MensagemRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    void seed() {
        if (repo.count() == 0) {
            repo.save(new Mensagem("Hello World do AcolheSP, direto do PostgreSQL!"));
        }
    }

    @GetMapping("/hello-world")
    public Map<String, String> hello() {
        log.info("[1/3] Requisicao recebida do frontend em /api/hello-world");
        String texto = repo.findAll().stream().findFirst()
                .map(Mensagem::getTexto)
                .orElse("Sem dados no banco");
        log.info("[2/3] Mensagem lida do PostgreSQL: {}", texto);
        log.info("[3/3] Respondendo ao frontend em JSON");
        return Map.of("mensagem", texto);
    }
}
