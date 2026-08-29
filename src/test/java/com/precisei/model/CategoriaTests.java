package com.precisei.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class CategoriaTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void nomeEmBrancoDeveSerInvalido() {
        Categoria categoria = new Categoria("   ");

        assertTrue(validator.validate(categoria).stream()
                .anyMatch(violacao -> "nome".equals(violacao.getPropertyPath().toString())));
    }
}
