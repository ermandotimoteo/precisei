package com.precisei.config;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class CorrelacaoFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String id = UUID.randomUUID().toString();
        try {
            MDC.put("correlationId", id);
            res.setHeader("X-Correlation-Id", id);
            chain.doFilter(req, res);
        } finally { MDC.remove("correlationId"); }
    }
}
