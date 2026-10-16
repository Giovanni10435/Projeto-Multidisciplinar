package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoDetalheResponse;
import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class AbrigoServiceTest {

    @Autowired
    private AbrigoService abrigoService;

    @Autowired
    private InstituicaoRepository instituicaoRepo;

    @Autowired
    private ContaRepository contaRepo;

    @Autowired
    private EntityManager entityManager;

    private AbrigoRequest criarRequestValido() {
        return new AbrigoRequest(
                "yago@exemplo.com",
                "Abrigo do Yago da Silva",
                "37.002.252/0001-60",
                "Abrigo para acolhimento",
                "(11) 97300-6106",
                "yago@exemplo.com",
                "Rua do Yago, 236",
                "01234-567",
                BigDecimal.valueOf(-23.5505),
                BigDecimal.valueOf(-46.6333)
        );
    }

    private void sincronizarELimparContexto() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void deveCadastrarAbrigoComDadosValidos() {
        AbrigoRequest request = criarRequestValido();

        long totalContasAntes = contaRepo.count();
        long totalAbrigosAntes = instituicaoRepo.count();

        AbrigoResponse response = abrigoService.cadastrarAbrigo(request);

        sincronizarELimparContexto();

        assertNotNull(response.id());
        assertEquals(request.nome(), response.nome());
        assertEquals(request.emailContato(), response.emailContato());

        Instituicao salva = instituicaoRepo.findById(response.id())
                .orElseThrow();

        assertEquals(request.email(), salva.getEmail());
        assertEquals(request.nome(), salva.getNome());
        assertEquals(request.cnpj(), salva.getCnpj());
        assertEquals(request.descricao(), salva.getDescricao());
        assertEquals(request.telefone(), salva.getTelefone());
        assertEquals(request.emailContato(), salva.getEmailContato());

        assertNotNull(salva.getLocalizacao());
        assertEquals(
                request.endereco(),
                salva.getLocalizacao().getEndereco()
        );
        assertEquals(
                request.cep(),
                salva.getLocalizacao().getCep()
        );

        assertEquals(
                0,
                request.latitude().compareTo(
                        salva.getLocalizacao().getLatitude()
                )
        );
        assertEquals(
                0,
                request.longitude().compareTo(
                        salva.getLocalizacao().getLongitude()
                )
        );

        assertEquals(totalContasAntes + 1, contaRepo.count());
        assertEquals(totalAbrigosAntes + 1, instituicaoRepo.count());
    }

    @Test
    void naoDeveCadastrarAbrigoSemNome() {
        AbrigoRequest request = new AbrigoRequest(
                "yago@exemplo.com",
                null,
                "37.002.252/0001-60",
                "Abrigo para acolhimento",
                "(11) 97300-6106",
                "yago@exemplo.com",
                "Rua do Yago, 236",
                "01234-567",
                BigDecimal.valueOf(-23.5505),
                BigDecimal.valueOf(-46.6333)
        );

        long totalContasAntes = contaRepo.count();
        long totalAbrigosAntes = instituicaoRepo.count();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.cadastrarAbrigo(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(totalContasAntes, contaRepo.count());
        assertEquals(totalAbrigosAntes, instituicaoRepo.count());
        assertFalse(contaRepo.existsByEmail(request.email()));
    }

    @Test
    void naoDeveCadastrarAbrigoSemCoordenadas() {
        AbrigoRequest request = new AbrigoRequest(
                "yago@exemplo.com",
                "Abrigo do Yago da Silva",
                "37.002.252/0001-60",
                "Abrigo para acolhimento",
                "(11) 97300-6106",
                "yago@exemplo.com",
                "Rua do Yago, 236",
                "01234-567",
                null,
                null
        );

        long totalContasAntes = contaRepo.count();
        long totalAbrigosAntes = instituicaoRepo.count();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.cadastrarAbrigo(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(totalContasAntes, contaRepo.count());
        assertEquals(totalAbrigosAntes, instituicaoRepo.count());
        assertFalse(contaRepo.existsByEmail(request.email()));
    }

    @Test
    void naoDeveCadastrarAbrigoComEmailJaExistente() {
        AbrigoRequest primeiroRequest = criarRequestValido();

        abrigoService.cadastrarAbrigo(primeiroRequest);
        sincronizarELimparContexto();

        // Mesmo e-mail, mas sem repetir o CNPJ:
        // a intenção é isolar a regra de duplicidade do e-mail.
        AbrigoRequest requestDuplicado = new AbrigoRequest(
                primeiroRequest.email(),
                "Outro abrigo",
                null,
                primeiroRequest.descricao(),
                primeiroRequest.telefone(),
                primeiroRequest.emailContato(),
                primeiroRequest.endereco(),
                primeiroRequest.cep(),
                primeiroRequest.latitude(),
                primeiroRequest.longitude()
        );

        long totalContasAntes = contaRepo.count();
        long totalAbrigosAntes = instituicaoRepo.count();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.cadastrarAbrigo(requestDuplicado)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(totalContasAntes, contaRepo.count());
        assertEquals(totalAbrigosAntes, instituicaoRepo.count());
    }

    @Test
    void naoDeveCadastrarAbrigoComCnpjJaExistente() {
        AbrigoRequest primeiroRequest = criarRequestValido();

        abrigoService.cadastrarAbrigo(primeiroRequest);
        sincronizarELimparContexto();

        // Outro e-mail para que a execução chegue à validação do CNPJ.
        AbrigoRequest requestDuplicado = new AbrigoRequest(
                "outro.yago@exemplo.com",
                "Outro abrigo",
                primeiroRequest.cnpj(),
                primeiroRequest.descricao(),
                primeiroRequest.telefone(),
                "outro.yago@exemplo.com",
                primeiroRequest.endereco(),
                primeiroRequest.cep(),
                primeiroRequest.latitude(),
                primeiroRequest.longitude()
        );

        long totalContasAntes = contaRepo.count();
        long totalAbrigosAntes = instituicaoRepo.count();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.cadastrarAbrigo(requestDuplicado)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(totalContasAntes, contaRepo.count());
        assertEquals(totalAbrigosAntes, instituicaoRepo.count());
        assertFalse(contaRepo.existsByEmail(requestDuplicado.email()));
    }

    @Test
    void deveDetalharAbrigoExistente() {
        AbrigoResponse cadastrado =
                abrigoService.cadastrarAbrigo(criarRequestValido());

        sincronizarELimparContexto();

        AbrigoDetalheResponse response =
                abrigoService.detalharAbrigo(cadastrado.id());

        assertEquals(cadastrado.id(), response.abrigo().id());
        assertEquals(
                "Abrigo do Yago da Silva",
                response.abrigo().nome()
        );

        // O abrigo foi criado neste teste, sem necessidades associadas.
        assertTrue(response.necessidades().isEmpty());
    }

    @Test
    void naoDeveDetalharAbrigoInexistente() {
        // IDs gerados pelo banco são positivos neste esquema.
        // A consulta confirma a ausência antes de testar o service.
        Integer idInexistente = -1;

        assertFalse(instituicaoRepo.existsById(idInexistente));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.detalharAbrigo(idInexistente)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deveAtualizarNomeDoAbrigo() {
        AbrigoRequest requestOriginal = criarRequestValido();

        AbrigoResponse cadastrado =
                abrigoService.cadastrarAbrigo(requestOriginal);

        sincronizarELimparContexto();

        AbrigoRequest requestAtualizacao = new AbrigoRequest(
                null,
                "Nome novo",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        AbrigoResponse response = abrigoService.atualizarAbrigo(
                cadastrado.id(),
                requestAtualizacao
        );

        sincronizarELimparContexto();

        Instituicao atualizada = instituicaoRepo.findById(cadastrado.id())
                .orElseThrow();

        assertEquals("Nome novo", response.nome());
        assertEquals("Nome novo", atualizada.getNome());

        // Atualizar o nome não deve apagar os demais dados.
        assertEquals(requestOriginal.email(), atualizada.getEmail());
        assertEquals(requestOriginal.cnpj(), atualizada.getCnpj());
        assertEquals(
                requestOriginal.emailContato(),
                atualizada.getEmailContato()
        );
        assertNotNull(atualizada.getLocalizacao());
        assertEquals(
                requestOriginal.endereco(),
                atualizada.getLocalizacao().getEndereco()
        );
    }
}