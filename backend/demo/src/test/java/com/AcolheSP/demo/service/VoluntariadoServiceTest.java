package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.SolicitacaoResponse;
import com.AcolheSP.demo.dto.VoluntariadoRequest;
import com.AcolheSP.demo.model.Instituicao;
import com.AcolheSP.demo.model.StatusVoluntariado;
import com.AcolheSP.demo.model.Voluntariado;
import com.AcolheSP.demo.model.Voluntario;
import com.AcolheSP.demo.repository.VoluntariadoRepository;

@SpringBootTest
@Transactional
class VoluntariadoServiceTest {

    @Autowired
    private EntityManager em;

    private static String token() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID() + "@exemplo.com";
    }

    private Voluntario criarVoluntario() {
        Voluntario v = new Voluntario();
        v.setEmail(emailUnico("voluntario"));
        v.setRole("usuario");
        v.setNome("Ana Teste");
        v.setTelefone("(11) 11111-1111");
        v.setDisponibilidade("Sábados");
        em.persist(v);
        em.flush();
        return v;
    }

    // Abrigo sem localização (para testes que não precisam de mapa)
    private Instituicao criarInstituicao(String nome) {
        Instituicao i = new Instituicao();
        i.setNome(nome);

        em.persist(i);
        em.flush();
        
        return i;
    }

    // dataSolicitacao null = a entidade preenche com a data de hoje
    private Voluntariado criarVoluntariado(Voluntario v, Instituicao inst, StatusVoluntariado status,
            LocalDate dataSolicitacao) {
        Voluntariado vt = new Voluntariado();
        vt.setVoluntario(v);
        vt.setInstituicao(inst);
        vt.setStatus(status);
        vt.setDataSolicitacao(dataSolicitacao);
        em.persist(vt);
        em.flush();
        return vt;
    }

    private void sincronizar() {
        em.flush();
        em.clear();
    }

    @Autowired
    private VoluntariadoService service;

    @Autowired
    private VoluntariadoRepository voluntariadoRepo;

    private Voluntario voluntario;
    private Instituicao inst;

    @BeforeEach
    void criarDados() {
        voluntario = criarVoluntario();
        inst = criarInstituicao("Casa " + token());
    }

    private ResponseStatusException erro(VoluntariadoRequest req) {
        return assertThrows(ResponseStatusException.class, () -> service.demonstrarInteresse(inst.getId(), req));
    }

    @Test
    void deveDemonstrarInteresseComStatusPendenteESalvarNoBanco() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 12, 1);

        SolicitacaoResponse res = service.demonstrarInteresse(inst.getId(),
                new VoluntariadoRequest(voluntario.getId(), inicio, fim, null, null));

        assertEquals("VOLUNTARIADO", res.tipo());
        assertEquals("PENDENTE", res.status());
        assertEquals(voluntario.getId(), res.voluntarioId());
        assertEquals("Ana Teste", res.voluntarioNome());
        assertEquals(inicio, res.dataInicio());
        assertEquals(fim, res.dataFim());
        assertNull(res.necessidadeId());

        sincronizar();
        assertEquals(1, voluntariadoRepo.findByInstituicaoId(inst.getId()).size());
    }

    @Test
    void deveAtualizarContatoEDisponibilidadeDoVoluntarioQuandoInformados() {
        service.demonstrarInteresse(inst.getId(),
                new VoluntariadoRequest(voluntario.getId(), null, null, "(11) 99999-9999", "Domingos"));

        assertEquals("(11) 99999-9999", voluntario.getTelefone());
        assertEquals("Domingos", voluntario.getDisponibilidade());
    }

    @Test
    void naoDeveSobrescreverContatoQuandoVemEmBranco() {
        service.demonstrarInteresse(inst.getId(),
                new VoluntariadoRequest(voluntario.getId(), null, null, "  ", null));

        assertEquals("(11) 11111-1111", voluntario.getTelefone());
        assertEquals("Sábados", voluntario.getDisponibilidade());
    }

    @Test
    void naoDeveDemonstrarInteresseSemVoluntarioId() {
        ResponseStatusException ex = erro(new VoluntariadoRequest(null, null, null, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: voluntarioId", ex.getReason());
    }

    @Test
    void naoDeveAceitarDataFimAntesDeDataInicio() {
        ResponseStatusException ex = erro(new VoluntariadoRequest(voluntario.getId(),
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1), null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("dataFim não pode ser antes de dataInicio", ex.getReason());
    }

    @Test
    void naoDeveDemonstrarInteresseEmAbrigoInexistente() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.demonstrarInteresse(Integer.MAX_VALUE,
                        new VoluntariadoRequest(voluntario.getId(), null, null, null, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Abrigo não encontrado", ex.getReason());
    }

    @Test
    void naoDeveDemonstrarInteresseComVoluntarioInexistente() {
        ResponseStatusException ex = erro(new VoluntariadoRequest(Integer.MAX_VALUE, null, null, null, null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Voluntário não encontrado", ex.getReason());
    }

    @Test
    void naoDevePermitirDuasCandidaturasAtivasNoMesmoAbrigo() {
        service.demonstrarInteresse(inst.getId(),
                new VoluntariadoRequest(voluntario.getId(), null, null, null, null));

        ResponseStatusException ex = erro(new VoluntariadoRequest(voluntario.getId(), null, null, null, null));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void devePermitirNovaCandidaturaDepoisDaAnteriorFinalizada() {
        criarVoluntariado(voluntario, inst, StatusVoluntariado.FINALIZADO, null);

        SolicitacaoResponse res = service.demonstrarInteresse(inst.getId(),
                new VoluntariadoRequest(voluntario.getId(), null, null, null, null));

        assertEquals("PENDENTE", res.status());
    }

    @Test
    void devePermitirCandidaturaDoMesmoVoluntarioEmOutroAbrigo() {
        service.demonstrarInteresse(inst.getId(),
                new VoluntariadoRequest(voluntario.getId(), null, null, null, null));
        Instituicao outro = criarInstituicao("Casa " + token());

        SolicitacaoResponse res = service.demonstrarInteresse(outro.getId(),
                new VoluntariadoRequest(voluntario.getId(), null, null, null, null));

        assertEquals("PENDENTE", res.status());
    }
}