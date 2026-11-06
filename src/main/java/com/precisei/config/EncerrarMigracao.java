package com.precisei.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("migrate")
public class EncerrarMigracao implements CommandLineRunner {
    private final ConfigurableApplicationContext context;
    public EncerrarMigracao(ConfigurableApplicationContext context) { this.context = context; }
    @Override public void run(String... args) {
        org.slf4j.LoggerFactory.getLogger(getClass()).info("event=migration_completed");
        context.close();
    }
}
