package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.dto.VoluntariadoRequest;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.Voluntariado;
import com.AcolheSP.demo.model.Voluntario;
import com.AcolheSP.demo.repository.InstituicaoRepository;
import com.AcolheSP.demo.repository.VoluntariadoRepository;
import com.AcolheSP.demo.repository.VoluntarioRepository;

@ExtendWith(MockitoExtension.class)
class VoluntariadoServiceTest {

    private static final Integer INST_ID = 10;
    private static final Integer VOL_ID = 5;

    @Mock
    private VoluntariadoRepository voluntariadoRepo;

    @Mock
    private VoluntarioRepository voluntarioRepo;

    @Mock
    private InstituicaoRepository instituicaoRepo;

    private VoluntariadoService service;
    private Voluntario voluntario;
    private Instituicao instituicao;

    @BeforeEach
    void setUp() {
        service = new VoluntariadoService(voluntariadoRepo, voluntarioRepo, instituicaoRepo);

        voluntario = new Voluntario();
        voluntario.setId(VOL_ID);
        voluntario.setNome("Ana");
        voluntario.setTelefone("(11) 11111-1111");
        voluntario.setDisponibilidade("Domingos");

        instituicao = new Instituicao();
        instituicao.setId(INST_ID);
    }

    private void cenarioValido(boolean candidaturaAtiva) {
        when(instituicaoRepo.findById(INST_ID)).thenReturn(Optional.of(instituicao));
        when(voluntarioRepo.findById(VOL_ID)).thenReturn(Optional.of(voluntario));
        when(voluntariadoRepo.existsByVoluntarioIdAndInstituicaoIdAndStatusIn(
                eq(VOL_ID), eq(INST_ID), anyCollection())).thenReturn(candidaturaAtiva);
    }

    private void salvarDevolvendoComId() {
        when(voluntariadoRepo.save(any(Voluntariado.class))).thenAnswer(inv -> {
            Voluntariado v = inv.getArgument(0);
            v.setId(3);
            return v;
        });
    }

    private ResponseStatusException erro(VoluntariadoRequest req) {
        return assertThrows(ResponseStatusException.class, () -> service.demonstrarInteresse(INST_ID, req));
    }

    @Test
    void deveDemonstrarInteresseComStatusPendente() {
        cenarioValido(false);
        salvarDevolvendoComId();
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 12, 1);

        SolicitacaoResponse res = service.demonstrarInteresse(INST_ID,
                new VoluntariadoRequest(VOL_ID, inicio, fim, null, null));

        assertEquals(3, res.id());
        assertEquals("VOLUNTARIADO", res.tipo());
        assertEquals("PENDENTE", res.status());
        assertEquals(VOL_ID, res.voluntarioId());
        assertEquals("Ana", res.voluntarioNome());
        assertEquals(inicio, res.dataInicio());
        assertEquals(fim, res.dataFim());
        assertNull(res.necessidadeId());
    }

    @Test
    void deveAtualizarContatoEDisponibilidadeDoVoluntarioQuandoInformados() {
        cenarioValido(false);
        salvarDevolvendoComId();

        service.demonstrarInteresse(INST_ID,
                new VoluntariadoRequest(VOL_ID, null, null, "(11) 99999-9999", "Sábados"));

        assertEquals("(11) 99999-9999", voluntario.getTelefone());
        assertEquals("Sábados", voluntario.getDisponibilidade());
    }

    @Test
    void naoDeveSobrescreverContatoQuandoVemEmBranco() {
        cenarioValido(false);
        salvarDevolvendoComId();

        service.demonstrarInteresse(INST_ID, new VoluntariadoRequest(VOL_ID, null, null, "  ", null));

        assertEquals("(11) 11111-1111", voluntario.getTelefone());
        assertEquals("Domingos", voluntario.getDisponibilidade());
    }

    @Test
    void naoDeveDemonstrarInteresseSemVoluntarioId() {
        ResponseStatusException ex = erro(new VoluntariadoRequest(null, null, null, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: voluntarioId", ex.getReason());
        verify(voluntariadoRepo, never()).save(any());
    }

    @Test
    void naoDeveAceitarDataFimAntesDeDataInicio() {
        ResponseStatusException ex = erro(new VoluntariadoRequest(VOL_ID,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1), null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("dataFim não pode ser antes de dataInicio", ex.getReason());
    }

    @Test
    void naoDeveDemonstrarInteresseEmAbrigoInexistente() {
        when(instituicaoRepo.findById(INST_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = erro(new VoluntariadoRequest(VOL_ID, null, null, null, null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Abrigo não encontrado", ex.getReason());
    }

    @Test
    void naoDeveDemonstrarInteresseComVoluntarioInexistente() {
        when(instituicaoRepo.findById(INST_ID)).thenReturn(Optional.of(instituicao));
        when(voluntarioRepo.findById(VOL_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = erro(new VoluntariadoRequest(VOL_ID, null, null, null, null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Voluntário não encontrado", ex.getReason());
    }

    @Test
    void naoDevePermitirDuasCandidaturasAtivasNoMesmoAbrigo() {
        cenarioValido(true);

        ResponseStatusException ex = erro(new VoluntariadoRequest(VOL_ID, null, null, "(11) 99999-9999", null));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(voluntariadoRepo, never()).save(any());
        assertEquals("(11) 11111-1111", voluntario.getTelefone());
    }
}