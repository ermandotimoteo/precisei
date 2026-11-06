package com.precisei.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FiltroLoginTests {
    @Test void espacosECaixaDoEmailNaoContornamLimitePorConta() throws Exception {
        var filtro = new FiltroLogin(new LimiteLogin());
        assertEquals(chave(filtro, "  PESSOA@example.invalid  "), chave(filtro, "pessoa@example.invalid"));
    }
    private Object chave(FiltroLogin filtro, String email) throws Exception {
        var req = new MockHttpServletRequest("POST", "/login");
        req.setParameter("tipo", "CLIENTE");
        req.setParameter("username", email);
        filtro.doFilter(req, new MockHttpServletResponse(), new MockFilterChain());
        return req.getAttribute("loginAccountKey");
    }
}
