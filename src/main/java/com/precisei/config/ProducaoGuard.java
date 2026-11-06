package com.precisei.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProducaoGuard {
    private final Environment env;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    public ProducaoGuard(Environment env, org.springframework.jdbc.core.JdbcTemplate jdbc) { this.env = env; this.jdbc = jdbc; }
    @PostConstruct void validar() {
        String user = env.getProperty("spring.datasource.username", "");
        if (user.isBlank() || user.equalsIgnoreCase("root")) throw new IllegalStateException("Produção exige usuário de banco restrito.");
        if (env.getProperty("spring.datasource.password", "").isBlank()) throw new IllegalStateException("Produção exige credenciais externas.");
        String adminEmail = env.getProperty("app.security.admin-email", "");
        String adminHash = env.getProperty("app.security.admin-password-hash", "");
        if (adminEmail.isBlank() || !adminEmail.contains("@")
                || !(adminHash.startsWith("{argon2id}$argon2id$") || adminHash.matches("(?:\\{bcrypt})?\\$2[aby]\\$\\d{2}\\$.{53}"))) {
            throw new IllegalStateException("Configure e-mail e hash válidos para a manutenção restrita antes de iniciar produção.");
        }
        Integer demos = jdbc.queryForObject("select (select count(*) from clientes where email like '%.demo@precisei.local') + (select count(*) from profissionais where email like '%.demo@precisei.local')", Integer.class);
        if (demos != null && demos > 0) throw new IllegalStateException("Há contas demonstrativas: reconcilie a base antes de operar em produção.");
    }
}
