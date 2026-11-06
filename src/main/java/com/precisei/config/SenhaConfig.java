package com.precisei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SenhaConfig {

    @Bean
    org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    @Bean
    PasswordEncoder passwordEncoder(@org.springframework.beans.factory.annotation.Value("${app.security.bcrypt-strength:12}") int strength) {
        return criarEncoder(strength);
    }

    public static PasswordEncoder criarEncoder(int strength) {
        var bcrypt = new BCryptPasswordEncoder(strength);
        var argon2 = new org.springframework.security.crypto.argon2.Argon2PasswordEncoder(16, 32, 1, 19456, 2);
        var encoder = new org.springframework.security.crypto.password.DelegatingPasswordEncoder("argon2id",
                java.util.Map.of("argon2id", argon2, "bcrypt", bcrypt));
        encoder.setDefaultPasswordEncoderForMatches(bcrypt);
        return encoder;
    }
}
