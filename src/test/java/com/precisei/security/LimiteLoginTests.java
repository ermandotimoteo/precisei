package com.precisei.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LimiteLoginTests {
    @Test void bloqueiaAposDezFalhasELiberaNoSucesso() {
        LimiteLogin limite = new LimiteLogin();
        for (int i = 0; i < 9; i++) limite.falhou("origem");
        assertFalse(limite.bloqueado("origem"));
        limite.falhou("origem"); assertTrue(limite.bloqueado("origem"));
        assertFalse(limite.bloqueado("outra-origem"));
        limite.sucesso("origem"); assertFalse(limite.bloqueado("origem"));
    }
}
