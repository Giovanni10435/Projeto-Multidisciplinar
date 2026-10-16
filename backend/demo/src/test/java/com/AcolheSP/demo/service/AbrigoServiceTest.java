package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.AbrigoDetalheResponse;
import com.AcolheSP.demo.dto.AbrigoRequest;
import com.AcolheSP.demo.dto.AbrigoResponse;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Localizacao;
import com.AcolheSP.demo.model.StatusNecessidade;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.NecessidadeRepository;

@ExtendWith(MockitoExtension.class)
class AbrigoServiceTest {

    @Mock
    private InstituicaoRepository instituicaoRepo;

    @Mock
    private ContaRepository contaRepo;

    @Mock
    private NecessidadeRepository necessidadeRepo;

    @InjectMocks
    private AbrigoService abrigoService;

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

    @Test
    void deveCadastrarAbrigoComDadosValidos() {
        AbrigoRequest request = criarRequestValido();

        when(contaRepo.existsByEmail(request.email()))
                .thenReturn(false);

        when(instituicaoRepo.existsByCnpj(request.cnpj()))
                .thenReturn(false);

        when(instituicaoRepo.save(any(Instituicao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbrigoResponse response = abrigoService.cadastrarAbrigo(request);

        assertEquals(request.nome(), response.nome());
        assertEquals(request.emailContato(), response.emailContato());

        verify(instituicaoRepo).save(any(Instituicao.class));
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

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.cadastrarAbrigo(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(instituicaoRepo, never()).save(any());
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

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.cadastrarAbrigo(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(instituicaoRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarAbrigoComEmailJaExistente() {
        AbrigoRequest request = criarRequestValido();

        when(contaRepo.existsByEmail(request.email()))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.cadastrarAbrigo(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(instituicaoRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarAbrigoComCnpjJaExistente() {
        AbrigoRequest request = criarRequestValido();

        when(contaRepo.existsByEmail(request.email()))
                .thenReturn(false);

        when(instituicaoRepo.existsByCnpj(request.cnpj()))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.cadastrarAbrigo(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(instituicaoRepo, never()).save(any());
    }

    @Test
    void deveDetalharAbrigoExistente() {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(1);
        instituicao.setNome("Abrigo do Yago da Silva");
        instituicao.setLocalizacao(new Localizacao());

        when(instituicaoRepo.findById(1))
                .thenReturn(Optional.of(instituicao));

        when(necessidadeRepo.findByInstituicaoIdAndStatus(
                1, StatusNecessidade.ABERTA))
                .thenReturn(List.of());

        AbrigoDetalheResponse response = abrigoService.detalharAbrigo(1);

        assertEquals(
                "Abrigo do Yago da Silva",
                response.abrigo().nome()
        );
        assertEquals(0, response.necessidades().size());
    }

    @Test
    void naoDeveDetalharAbrigoInexistente() {
        when(instituicaoRepo.findById(999))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> abrigoService.detalharAbrigo(999)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deveAtualizarNomeDoAbrigo() {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(1);
        instituicao.setNome("Nome antigo");
        instituicao.setLocalizacao(new Localizacao());

        AbrigoRequest request = new AbrigoRequest(
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

        when(instituicaoRepo.findById(1))
                .thenReturn(Optional.of(instituicao));

        when(instituicaoRepo.save(any(Instituicao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbrigoResponse response = abrigoService.atualizarAbrigo(1, request);

        assertEquals("Nome novo", response.nome());
        verify(instituicaoRepo).save(instituicao);
    }
}