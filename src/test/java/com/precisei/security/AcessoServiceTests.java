package com.precisei.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;

class AcessoServiceTests {
    private final AcessoService acesso = new AcessoService();
    @AfterEach void limpar() { SecurityContextHolder.clearContext(); }
    static void autenticar(Long id, TipoConta tipo) {
        var user = new UsuarioAutenticado(id, tipo, "Teste");
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }
    @Test void anonimoNaoTemDono() { assertThrows(AccessDeniedException.class, () -> acesso.exigirDono(TipoConta.CLIENTE, 1L)); }
    @Test void clienteNaoEProfissionalMesmoComMesmoId() {
        autenticar(1L, TipoConta.CLIENTE);
        assertDoesNotThrow(() -> acesso.exigirDono(TipoConta.CLIENTE, 1L));
        assertThrows(AccessDeniedException.class, () -> acesso.exigirDono(TipoConta.PROFISSIONAL, 1L));
        assertThrows(AccessDeniedException.class, () -> acesso.exigirDono(TipoConta.CLIENTE, 2L));
    }
    @Test void profissionalNaoPodeSePassarPorCliente() {
        autenticar(1L, TipoConta.PROFISSIONAL);
        assertThrows(AccessDeniedException.class, acesso::clienteAtual);
    }
}
