package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.DoacaoRequest;
import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.model.Doacao;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Necessidade;
import com.AcolheSP.demo.model.StatusDoacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.model.Voluntario;
import com.AcolheSP.demo.repository.DoacaoRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class DoacaoServiceTest {

    @Autowired
    private DoacaoService doacaoService;

    @Autowired
    private DoacaoRepository doacaoRepo;

    @Autowired
    private EntityManager entityManager;

    private Voluntario criarVoluntario() {
        Voluntario voluntario = new Voluntario();
        voluntario.setEmail(
                "yago.voluntario." + UUID.randomUUID() + "@exemplo.com"
        );
        voluntario.setRole("usuario");
        voluntario.setNome("Yago da Silva");
        voluntario.setTelefone("(11) 97300-6106");
        voluntario.setDisponibilidade("Sábados de manhã");

        entityManager.persist(voluntario);
        entityManager.flush();

        return voluntario;
    }

    private Necessidade criarNecessidade(StatusNecessidade status) {
        Instituicao instituicao = new Instituicao();
        instituicao.setEmail(
                "yago.abrigo." + UUID.randomUUID() + "@exemplo.com"
        );
        instituicao.setRole("abrigo");
        instituicao.setNome("Abrigo do Yago da Silva");
        instituicao.setDescricao("Abrigo para acolhimento");
        instituicao.setTelefone("(11) 97300-6106");
        instituicao.setEmailContato("yago@exemplo.com");

        entityManager.persist(instituicao);

        Necessidade necessidade = new Necessidade();
        necessidade.setDescricao("Cobertores para teste de doação");
        necessidade.setQuantidadeSolicitada(50);
        necessidade.setQuantidadeRecebida(0);
        necessidade.setStatus(status);
        necessidade.setInstituicao(instituicao);

        entityManager.persist(necessidade);
        entityManager.flush();

        return necessidade;
    }

    private void sincronizarELimparContexto() {
        entityManager.flush();
        entityManager.clear();
    }

    private void verificarRejeicao(
            DoacaoRequest request,
            HttpStatus statusEsperado,
            String motivoEsperado
    ) {
        long totalAntes = doacaoRepo.count();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> doacaoService.realizarDoacao(request)
        );

        assertEquals(statusEsperado, exception.getStatusCode());
        assertEquals(motivoEsperado, exception.getReason());
        assertEquals(totalAntes, doacaoRepo.count());
    }

    @Test
    void deveRealizarDoacaoComDadosValidos() {
        Voluntario voluntario = criarVoluntario();
        Necessidade necessidade =
                criarNecessidade(StatusNecessidade.ABERTA);

        Integer voluntarioId = voluntario.getId();
        Integer necessidadeId = necessidade.getId();

        DoacaoRequest request = new DoacaoRequest(
                voluntarioId,
                necessidadeId,
                5
        );

        long totalAntes = doacaoRepo.count();

        SolicitacaoResponse response =
                doacaoService.realizarDoacao(request);

        sincronizarELimparContexto();

        assertNotNull(response.id());
        assertEquals("DOACAO", response.tipo());
        assertEquals("PENDENTE", response.status());
        assertEquals(voluntarioId, response.voluntarioId());
        assertEquals("Yago da Silva", response.voluntarioNome());
        assertEquals(
                "(11) 97300-6106",
                response.voluntarioTelefone()
        );
        assertEquals(
                "Sábados de manhã",
                response.voluntarioDisponibilidade()
        );
        assertEquals(necessidadeId, response.necessidadeId());
        assertEquals(
                "Cobertores para teste de doação",
                response.necessidadeDescricao()
        );
        assertEquals(Integer.valueOf(5), response.quantidade());
        assertNotNull(response.data());
        assertNull(response.dataInicio());
        assertNull(response.dataFim());

        Doacao salva = doacaoRepo.findById(response.id())
                .orElseThrow();

        assertEquals(Integer.valueOf(5), salva.getQuantidade());
        assertEquals(StatusDoacao.PENDENTE, salva.getStatus());
        assertEquals(voluntarioId, salva.getVoluntario().getId());
        assertEquals(necessidadeId, salva.getNecessidade().getId());
        assertEquals(response.data(), salva.getData());
        assertEquals(totalAntes + 1, doacaoRepo.count());

        Necessidade necessidadeDepois = entityManager.find(
                Necessidade.class,
                necessidadeId
        );

        // Uma doação pendente ainda não representa recebimento.
        assertEquals(
                Integer.valueOf(0),
                necessidadeDepois.getQuantidadeRecebida()
        );
        assertEquals(
                StatusNecessidade.ABERTA,
                necessidadeDepois.getStatus()
        );
    }

    @Test
    void naoDeveRealizarDoacaoSemVoluntarioId() {
        // Os outros valores não são consultados:
        // a primeira validação deve rejeitar voluntarioId nulo.
        DoacaoRequest request = new DoacaoRequest(
                null,
                -1,
                5
        );

        verificarRejeicao(
                request,
                HttpStatus.BAD_REQUEST,
                "Campo obrigatório: voluntarioId"
        );
    }

    @Test
    void naoDeveRealizarDoacaoSemNecessidadeId() {
        // Os repositories só são consultados após as validações.
        DoacaoRequest request = new DoacaoRequest(
                -1,
                null,
                5
        );

        verificarRejeicao(
                request,
                HttpStatus.BAD_REQUEST,
                "Campo obrigatório: necessidadeId"
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1, -10})
    void naoDeveRealizarDoacaoComQuantidadeInvalida(
            Integer quantidade
    ) {
        DoacaoRequest request = new DoacaoRequest(
                -1,
                -1,
                quantidade
        );

        verificarRejeicao(
                request,
                HttpStatus.BAD_REQUEST,
                "quantidade deve ser maior que zero"
        );
    }

    @Test
    void naoDeveRealizarDoacaoComVoluntarioInexistente() {
        Necessidade necessidade =
                criarNecessidade(StatusNecessidade.ABERTA);

        Integer voluntarioIdInexistente = -1;

        assertNull(
                entityManager.find(
                        Voluntario.class,
                        voluntarioIdInexistente
                )
        );

        DoacaoRequest request = new DoacaoRequest(
                voluntarioIdInexistente,
                necessidade.getId(),
                5
        );

        verificarRejeicao(
                request,
                HttpStatus.NOT_FOUND,
                "Voluntário não encontrado"
        );
    }

    @Test
    void naoDeveRealizarDoacaoComNecessidadeInexistente() {
        Voluntario voluntario = criarVoluntario();

        Integer necessidadeIdInexistente = -1;

        assertNull(
                entityManager.find(
                        Necessidade.class,
                        necessidadeIdInexistente
                )
        );

        DoacaoRequest request = new DoacaoRequest(
                voluntario.getId(),
                necessidadeIdInexistente,
                5
        );

        verificarRejeicao(
                request,
                HttpStatus.NOT_FOUND,
                "Necessidade não encontrada"
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusNecessidade.class,
            names = {"ATENDIDA", "ENCERRADA"}
    )
    void naoDeveRealizarDoacaoParaNecessidadeNaoAberta(
            StatusNecessidade status
    ) {
        Voluntario voluntario = criarVoluntario();
        Necessidade necessidade = criarNecessidade(status);

        DoacaoRequest request = new DoacaoRequest(
                voluntario.getId(),
                necessidade.getId(),
                5
        );

        verificarRejeicao(
                request,
                HttpStatus.CONFLICT,
                "Essa necessidade não está mais aberta"
        );

        assertFalse(
                doacaoRepo.findAll().stream()
                        .anyMatch(doacao ->
                                necessidade.getId().equals(
                                        doacao.getNecessidade().getId()
                                )
                        )
        );
    }
}