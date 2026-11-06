package com.precisei.config;

import jakarta.servlet.DispatcherType;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.precisei.security.AutenticacaoProvider;
import com.precisei.security.FiltroLogin;
import com.precisei.security.LimiteLogin;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication(type = org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET)
public class SegurancaConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AutenticacaoProvider provider, LimiteLogin limite,
            org.springframework.security.core.session.SessionRegistry registry,
            @org.springframework.beans.factory.annotation.Value("${management.server.port:-1}") int managementPort) throws Exception {
        http.authenticationProvider(provider)
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                // Scrape permitido apenas no conector de gestão, publicado somente em loopback/rede privada.
                .requestMatchers(req -> managementPort > 0 && req.getLocalPort() == managementPort
                        && req.getRequestURI().equals("/actuator/prometheus")).permitAll()
                .requestMatchers(HttpMethod.GET, "/", "/login", "/cadastro/cliente", "/privacidade", "/css/**", "/error", "/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/servicos", "/profissionais").permitAll()
                .requestMatchers(HttpMethod.GET, "/clientes").hasAnyRole("CLIENTE", "MANUTENCAO")
                .requestMatchers(HttpMethod.POST, "/clientes/novo", "/profissionais/novo").permitAll()
                .requestMatchers("/servicos/**", "/servicos").hasRole("MANUTENCAO")
                .requestMatchers("/actuator/**").hasRole("MANUTENCAO")
                .requestMatchers("/solicitacoes/**", "/solicitacoes", "/profissionais/*/editar").authenticated()
                .anyRequest().denyAll())
            .formLogin(form -> form.loginPage("/login")
                .successHandler((req, res, auth) -> {
                    limite.sucesso(String.valueOf(req.getAttribute("loginAccountKey")));
                    LoggerFactory.getLogger("security.audit").info("event=auth_success principal={}", auth.getName());
                    res.sendRedirect(req.getContextPath() + "/solicitacoes");
                })
                .failureHandler((req, res, ex) -> {
                    limite.falhou("ip:" + req.getRemoteAddr());
                    limite.falhou(String.valueOf(req.getAttribute("loginAccountKey")));
                    LoggerFactory.getLogger("security.audit").warn("event=auth_failure");
                    res.sendRedirect(req.getContextPath() + "/login?erro");
                }).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?saiu").invalidateHttpSession(true).deleteCookies("JSESSIONID"))
            .sessionManagement(session -> session.sessionFixation(fix -> fix.changeSessionId())
                .maximumSessions(3).sessionRegistry(registry).expiredUrl("/login?expirou"))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; style-src 'self'; img-src 'self' data:; form-action 'self'; frame-ancestors 'none'; base-uri 'self'"))
                .referrerPolicy(policy -> policy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)))
            .addFilterBefore(new FiltroLogin(limite), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    org.springframework.security.web.session.HttpSessionEventPublisher sessionEvents() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }
}
