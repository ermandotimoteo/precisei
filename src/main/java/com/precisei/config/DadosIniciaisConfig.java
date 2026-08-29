package com.precisei.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.precisei.service.CategoriaService;

@Configuration(proxyBeanMethods = false)
public class DadosIniciaisConfig {

    @Bean
    CommandLineRunner carregarCategoriasIniciais(CategoriaService categoriaService) {
        return args -> categoriaService.garantirCategoriasPadrao(List.of(
                "Elétrica",
                "Faxina",
                "Hidráulica",
                "Montagem de móveis",
                "Chaveiro"));
    }
}
