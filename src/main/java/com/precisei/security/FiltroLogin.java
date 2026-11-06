package com.precisei.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

public class FiltroLogin extends OncePerRequestFilter {
    private final LimiteLogin limite;
    public FiltroLogin(LimiteLogin limite) { this.limite = limite; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (req.getMethod().equals("POST") && req.getRequestURI().equals(req.getContextPath() + "/login")) {
            String identity = String.valueOf(req.getParameter("tipo")) + "|"
                    + String.valueOf(req.getParameter("username")).trim().toLowerCase(java.util.Locale.ROOT);
            String key;
            try {
                key = "account:" + java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                        .digest(identity.trim().toLowerCase(java.util.Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
            req.setAttribute("loginAccountKey", key);
            if (limite.bloqueado("ip:" + req.getRemoteAddr()) || limite.bloqueado(key) || limite.bloqueado("overflow")) {
                res.setHeader("Retry-After", "900");
                res.sendError(429, "Aguarde antes de tentar novamente.");
                return;
            }
            chain.doFilter(new HttpServletRequestWrapper(req) {
                @Override public String getParameter(String name) {
                    if (name.equals("username")) return req.getParameter("tipo") + "|" + req.getParameter("username");
                    return super.getParameter(name);
                }
            }, res);
        } else chain.doFilter(req, res);
    }
}
