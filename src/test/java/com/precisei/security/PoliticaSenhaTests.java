package com.precisei.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaSenhaTests {
    @ParameterizedTest @ValueSource(strings = {"", "       ", "1234567"})
    void rejeitaSenhasCurtasOuVazias(String senha) { assertFalse(PoliticaSenha.valida(senha)); }
    @Test void rejeitaNulo() { assertFalse(PoliticaSenha.valida(null)); }
    @Test void aceitaLimiteInferior() { assertTrue(PoliticaSenha.valida("12345678")); }
    @Test void naoTruncaBytesMultibyte() {
        assertTrue(PoliticaSenha.valida("á".repeat(36)));
        assertFalse(PoliticaSenha.valida("á".repeat(37)));
        assertFalse(PoliticaSenha.valida("a".repeat(73)));
    }
}
