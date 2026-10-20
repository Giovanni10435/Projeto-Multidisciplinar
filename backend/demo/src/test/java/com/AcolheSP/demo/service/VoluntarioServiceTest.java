package com.AcolheSP.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.AcolheSP.demo.dto.VoluntarioRequest;
import com.AcolheSP.demo.dto.VoluntarioResponse;
import com.AcolheSP.demo.model.Voluntario;
import com.AcolheSP.demo.repository.ContaRepository;
import com.AcolheSP.demo.repository.VoluntarioRepository;

@ExtendWith(MockitoExtension.class)
class VoluntarioServiceTest {

    @Mock
    private VoluntarioRepository voluntarioRepo;

    @Mock
    private ContaRepository contaRepo;

    private final PasswordHasher hasher = new PasswordHasher();
    private VoluntarioService service;

    @BeforeEach
    void setUp() {
        service = new VoluntarioService(voluntarioRepo, contaRepo, hasher);
    }

    private void salvarDevolvendoComId() {
        when(voluntarioRepo.save(any(Voluntario.class))).thenAnswer(inv -> {
            Voluntario v = inv.getArgument(0);
            v.setId(10);
            return v;
        });
    }

    private ResponseStatusException cadastrarComErro(VoluntarioRequest req) {
        return assertThrows(ResponseStatusException.class, () -> service.cadastrar(req));
    }

    @Test
    void deveCadastrarVoluntarioComSenha() {
        when(contaRepo.existsByEmail("ana@exemplo.com")).thenReturn(false);
        salvarDevolvendoComId();

        VoluntarioResponse res = service.cadastrar(new VoluntarioRequest(
                "ana@exemplo.com", "Ana", "(11) 99999-0000", "Sábados", "senha123"));

        assertEquals(10, res.id());
        assertEquals("ana@exemplo.com", res.email());
        assertEquals("Ana", res.nome());
        assertEquals("(11) 99999-0000", res.telefone());
        assertEquals("Sábados", res.disponibilidade());

        ArgumentCaptor<Voluntario> captor = ArgumentCaptor.forClass(Voluntario.class);
        verify(voluntarioRepo).save(captor.capture());
        Voluntario salvo = captor.getValue();

        assertEquals("usuario", salvo.getRole());
        assertTrue(salvo.isAtivo());
        assertNotNull(salvo.getSenhaHash());
        assertNotEquals("senha123", salvo.getSenhaHash());
        assertTrue(hasher.confere("senha123", salvo.getSenhaHash()));
    }

    @Test
    void deveCadastrarVoluntarioSemSenhaEDeixarHashNulo() {
        when(contaRepo.existsByEmail("ana@exemplo.com")).thenReturn(false);
        salvarDevolvendoComId();

        service.cadastrar(new VoluntarioRequest("ana@exemplo.com", "Ana", null, null));

        ArgumentCaptor<Voluntario> captor = ArgumentCaptor.forClass(Voluntario.class);
        verify(voluntarioRepo).save(captor.capture());
        assertNull(captor.getValue().getSenhaHash());
    }

    @Test
    void naoDeveCadastrarSemEmail() {
        ResponseStatusException ex = cadastrarComErro(new VoluntarioRequest(null, "Ana", null, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: email", ex.getReason());
        verify(voluntarioRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarSemNome() {
        ResponseStatusException ex = cadastrarComErro(new VoluntarioRequest("ana@exemplo.com", "  ", null, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Campo obrigatório: nome", ex.getReason());
        verify(voluntarioRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarComEmailJaExistente() {
        when(contaRepo.existsByEmail("ana@exemplo.com")).thenReturn(true);

        ResponseStatusException ex = cadastrarComErro(new VoluntarioRequest("ana@exemplo.com", "Ana", null, null));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(voluntarioRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarComSenhaCurta() {
        when(contaRepo.existsByEmail("ana@exemplo.com")).thenReturn(false);

        ResponseStatusException ex = cadastrarComErro(
                new VoluntarioRequest("ana@exemplo.com", "Ana", null, null, "123"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(voluntarioRepo, never()).save(any());
    }

    @Test
    void deveBuscarVoluntarioExistente() {
        Voluntario v = new Voluntario();
        v.setId(5);
        v.setEmail("ana@exemplo.com");
        v.setNome("Ana");
        when(voluntarioRepo.findById(5)).thenReturn(Optional.of(v));

        VoluntarioResponse res = service.buscar(5);

        assertEquals(5, res.id());
        assertEquals("Ana", res.nome());
    }

    @Test
    void naoDeveBuscarVoluntarioInexistente() {
        when(voluntarioRepo.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.buscar(99));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}