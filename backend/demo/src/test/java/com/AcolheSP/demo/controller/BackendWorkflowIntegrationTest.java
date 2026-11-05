package com.AcolheSP.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalTime;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.AcolheSP.demo.model.*;
import com.AcolheSP.demo.service.PasswordHasher;

/** Fluxos reais com filtros de seguranca, controllers, services e banco isolado. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BackendWorkflowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired PasswordHasher hasher;
    private Conta gestor;
    private Conta outroGestor;
    private Instituicao abrigo;
    private Instituicao outroAbrigo;
    private Voluntario voluntario;
    private Voluntario outroVoluntario;
    private Necessidade necessidade;

    private String email() { return UUID.randomUUID() + "@exemplo.com"; }

    private Conta conta(String role) {
        Conta c = new Conta();
        c.setEmail(email());
        c.setRole(role);
        c.setSenhaHash(hasher.gerarHash("senha123"));
        em.persist(c);
        return c;
    }

    private Instituicao abrigo(Conta conta) {
        Instituicao i = new Instituicao();
        i.setNome("Casa teste");
        i.setConta(conta);
        em.persist(i);
        return i;
    }

    private Voluntario voluntario() {
        Voluntario v = new Voluntario();
        v.setEmail(email());
        v.setNome("Ana");
        v.setRole("usuario");
        v.setSenhaHash(hasher.gerarHash("senha123"));
        em.persist(v);
        return v;
    }

    @BeforeEach
    void preparar() {
        gestor = conta("abrigo");
        outroGestor = conta("abrigo");
        abrigo = abrigo(gestor);
        outroAbrigo = abrigo(outroGestor);
        voluntario = voluntario();
        outroVoluntario = voluntario();
        necessidade = new Necessidade();
        necessidade.setInstituicao(abrigo);
        necessidade.setDescricao("Cobertores");
        necessidade.setQuantidadeSolicitada(10);
        em.persist(necessidade);
        em.flush();
    }

    private MockHttpSession login(Conta conta) throws Exception {
        var resultado = mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + conta.getEmail() + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private String necessidades() { return "/api/instituicoes/" + abrigo.getId() + "/necessidades"; }
    private String horarios() { return "/api/instituicoes/" + abrigo.getId() + "/horarios-voluntariado"; }
    private String candidaturas() { return "/api/instituicoes/" + abrigo.getId() + "/voluntariados"; }
    private String solicitacoes() { return "/api/instituicoes/" + abrigo.getId() + "/solicitacoes"; }
    private String doacao(int quantidade) {
        return "{\"voluntarioId\":" + voluntario.getId() + ",\"necessidadeId\":"
                + necessidade.getId() + ",\"quantidade\":" + quantidade + "}";
    }
    private String horarioJson() {
        return "{\"dia\":\"Sábado\",\"funcao\":\"Cozinha\",\"inicio\":\"08:00\",\"fim\":\"12:00\"}";
    }
    private HorarioVoluntariado horario() {
        var h = new HorarioVoluntariado();
        h.setInstituicao(abrigo);
        h.setDia("Sábado"); h.setFuncao("Cozinha");
        h.setInicio(LocalTime.of(8, 0)); h.setFim(LocalTime.of(12, 0));
        em.persist(h); em.flush();
        return h;
    }

    @Test
    void loginRealCriaSessaoTrocaIdEPermiteMe() throws Exception {
        var sessao = new MockHttpSession();
        String idAnterior = sessao.getId();
        mvc.perform(post("/api/auth/login").session(sessao).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + gestor.getEmail().toUpperCase() + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.instituicaoId").value(abrigo.getId()));
        assertNotEquals(idAnterior, sessao.getId());
        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gestor.getId()));
        mvc.perform(post("/api/auth/logout").session(sessao).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void csrfRealObtidoNaApiPermiteLoginSemBypass() throws Exception {
        var resultado = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        String json = resultado.getResponse().getContentAsString();
        // O nome e o valor sao retornados pela propria API.
        var token = (org.springframework.security.web.csrf.CsrfToken)
                resultado.getRequest().getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        assertTrue(json.contains("token"));
        mvc.perform(post("/api/auth/login")
                .session((MockHttpSession) resultado.getRequest().getSession(false))
                .header(token.getHeaderName(), token.getToken()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + voluntario.getEmail() + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/doacoes")
                .session((MockHttpSession) resultado.getRequest().getSession(false))
                .header(token.getHeaderName(), token.getToken()).contentType(MediaType.APPLICATION_JSON)
                .content(doacao(1))).andExpect(status().isForbidden());
    }

    @Test
    void exigeSessaoECsrfParaEscritas() throws Exception {
        mvc.perform(post(necessidades()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"descricao\":\"Arroz\",\"quantidadeSolicitada\":5}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(necessidades()).session(login(gestor)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"descricao\":\"Arroz\",\"quantidadeSolicitada\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultasPublicasContinuamAcessiveis() throws Exception {
        mvc.perform(get("/api/abrigos")).andExpect(status().isOk());
        mvc.perform(get(necessidades())).andExpect(status().isOk());
        mvc.perform(get(horarios())).andExpect(status().isOk());
        mvc.perform(get(solicitacoes())).andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejeitaAbrigoSemVinculoESenhaErrada() throws Exception {
        Conta orfa = conta("abrigo"); em.flush();
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + orfa.getEmail() + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + gestor.getEmail() + "\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contaDesativadaPerdeAcessoMesmoComSessaoAnterior() throws Exception {
        var sessao = login(gestor);
        gestor.setAtivo(false); em.flush();
        mvc.perform(get(solicitacoes()).session(sessao)).andExpect(status().isForbidden());
    }

    @Test
    void gestorSoGerenciaNecessidadesDoSeuAbrigo() throws Exception {
        var sessao = login(gestor);
        mvc.perform(post(necessidades()).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"descricao\":\"Arroz\",\"quantidadeSolicitada\":5}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.descricao").value("Arroz"));
        mvc.perform(put(necessidades() + "/" + necessidade.getId()).session(sessao).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"descricao\":\"Cobertores novos\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete(necessidades() + "/" + necessidade.getId()).session(login(outroGestor)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post(necessidades()).session(login(voluntario)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void gestorCriaEditaRemoveHorarioSemApagarHistorico() throws Exception {
        var sessao = login(gestor);
        mvc.perform(post(horarios()).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(horarioJson())).andExpect(status().isCreated());
        var h = horario();
        mvc.perform(put(horarios() + "/" + h.getId()).session(sessao).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(horarioJson().replace("Cozinha", "Recepção")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.funcao").value("Recepção"));
        mvc.perform(delete(horarios() + "/" + h.getId()).session(sessao).with(csrf()))
                .andExpect(status().isNoContent());
        em.flush(); assertFalse(em.find(HorarioVoluntariado.class, h.getId()).isAtivo());
        mvc.perform(post(candidaturas()).session(login(voluntario)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"voluntarioId\":" + voluntario.getId()
                        + ",\"horarioId\":" + h.getId() + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void horarioInvalidoEOuDeOutroAbrigoSaoRejeitados() throws Exception {
        var sessao = login(gestor);
        mvc.perform(post(horarios()).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(horarioJson().replace("12:00", "07:00"))).andExpect(status().isBadRequest());
        mvc.perform(post(horarios()).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(horarioJson().replace("Sábado", "Feriado"))).andExpect(status().isBadRequest());
        mvc.perform(post(horarios()).session(login(outroGestor)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(horarioJson())).andExpect(status().isForbidden());
    }

    @Test
    void voluntarioSeCandidataAoHorarioESoPodeRepresentarASiMesmo() throws Exception {
        var h = horario();
        var sessao = login(voluntario);
        String json = "{\"voluntario_id\":" + voluntario.getId() + ",\"horario_id\":" + h.getId() + "}";
        mvc.perform(post(candidaturas()).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated()).andExpect(jsonPath("$.horarioId").value(h.getId()));
        mvc.perform(post(candidaturas()).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isConflict());
        mvc.perform(post(candidaturas()).session(login(outroVoluntario)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isForbidden());
        mvc.perform(post("/api/instituicoes/" + outroAbrigo.getId() + "/voluntariados")
                .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void doacaoRespeitaSaldoEEntregaContabilizadaUmaVez() throws Exception {
        var sessao = login(voluntario);
        mvc.perform(post("/api/doacoes").session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(doacao(8))).andExpect(status().isCreated());
        mvc.perform(post("/api/doacoes").session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(doacao(3))).andExpect(status().isConflict());
        Doacao doacao = em.createQuery("from Doacao where necessidade.id = :id", Doacao.class)
                .setParameter("id", necessidade.getId()).getSingleResult();
        var gestorSessao = login(gestor);
        String url = solicitacoes() + "/" + doacao.getId();
        mvc.perform(put(url).session(gestorSessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"DOACAO\",\"status\":\"ENTREGUE\"}"))
                .andExpect(status().isConflict());
        mvc.perform(put(url).session(gestorSessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"DOACAO\",\"status\":\"CONFIRMADA\"}"))
                .andExpect(status().isOk());
        mvc.perform(put(url).session(gestorSessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"DOACAO\",\"status\":\"ENTREGUE\"}"))
                .andExpect(status().isOk());
        assertEquals(8, necessidade.getQuantidadeRecebida());
        mvc.perform(put(url).session(gestorSessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"DOACAO\",\"status\":\"ENTREGUE\"}"))
                .andExpect(status().isConflict());
        assertEquals(8, necessidade.getQuantidadeRecebida());
    }

    @Test
    void voluntarioNaoPodeDoarEmNomeDeOutroNemExcluirNecessidadeComDoacao() throws Exception {
        mvc.perform(post("/api/doacoes").session(login(outroVoluntario)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(doacao(1))).andExpect(status().isForbidden());
        mvc.perform(post("/api/doacoes").session(login(voluntario)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(doacao(1))).andExpect(status().isCreated());
        mvc.perform(delete(necessidades() + "/" + necessidade.getId()).session(login(gestor)).with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void impedirReducaoDeMetaAbaixoDoComprometido() throws Exception {
        mvc.perform(post("/api/doacoes").session(login(voluntario)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(doacao(8))).andExpect(status().isCreated());
        mvc.perform(put(necessidades() + "/" + necessidade.getId()).session(login(gestor)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeSolicitada\":5}"))
                .andExpect(status().isConflict());
    }

    @Test
    void gestaoDeSolicitacoesRejeitaOutroAbrigo() throws Exception {
        mvc.perform(get(solicitacoes()).session(login(outroGestor))).andExpect(status().isForbidden());
        mvc.perform(put(solicitacoes() + "/123").session(login(voluntario)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"DOACAO\",\"status\":\"CONFIRMADA\"}"))
                .andExpect(status().isForbidden());
    }
    @Test
    void cancelamentoLiberaSaldoParaNovaDoacao() throws Exception {
        var usuario = login(voluntario);
        mvc.perform(post("/api/doacoes").session(usuario).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(doacao(10))).andExpect(status().isCreated());
        Doacao d = em.createQuery("from Doacao where necessidade.id = :id", Doacao.class)
                .setParameter("id", necessidade.getId()).getSingleResult();
        mvc.perform(put(solicitacoes() + "/" + d.getId()).session(login(gestor)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"DOACAO\",\"status\":\"RECUSADA\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/doacoes").session(usuario).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(doacao(10))).andExpect(status().isCreated());
        assertEquals(0, necessidade.getQuantidadeRecebida());
    }

    @Test
    void candidaturasEmHorariosDistintosESeguimentoDosStatus() throws Exception {
        var h1 = horario();
        var h2 = horario();
        var usuario = login(voluntario);
        for (var h : java.util.List.of(h1, h2)) {
            mvc.perform(post(candidaturas()).session(usuario).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).content("{\"voluntarioId\":" + voluntario.getId()
                            + ",\"horarioId\":" + h.getId() + "}"))
                    .andExpect(status().isCreated());
        }
        Voluntariado v = em.createQuery("from Voluntariado where horario.id = :id", Voluntariado.class)
                .setParameter("id", h1.getId()).getSingleResult();
        var sessao = login(gestor);
        String url = solicitacoes() + "/" + v.getId();
        mvc.perform(put(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"VOLUNTARIADO\",\"status\":\"FINALIZADO\"}"))
                .andExpect(status().isConflict());
        for (String status : java.util.List.of("APROVADO", "EM_ANDAMENTO", "FINALIZADO")) {
            mvc.perform(put(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"tipo\":\"VOLUNTARIADO\",\"status\":\"" + status + "\"}"))
                    .andExpect(status().isOk());
        }
        mvc.perform(put(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"VOLUNTARIADO\",\"status\":\"APROVADO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizacaoParcialSemDescricaoETextoInvalido() throws Exception {
        var sessao = login(gestor);
        String url = necessidades() + "/" + necessidade.getId();
        mvc.perform(put(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantidadeSolicitada\":20}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.descricao").value("Cobertores"));
        for (String descricao : java.util.List.of("   ", "a".repeat(256))) {
            mvc.perform(put(url).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"descricao\":\"" + descricao + "\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void cadastroNormalizaEmailEEvitaDuplicataComCapitalizacaoDiferente() throws Exception {
        mvc.perform(post("/api/voluntarios").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\" " + voluntario.getEmail().toUpperCase()
                        + " \",\"nome\":\"Outra Ana\",\"senha\":\"senha123\"}"))
                .andExpect(status().isConflict());
    }

}
