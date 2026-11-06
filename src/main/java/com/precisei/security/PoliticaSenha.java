package com.precisei.security;

import java.nio.charset.StandardCharsets;

public final class PoliticaSenha {
    private PoliticaSenha() { }
    public static boolean valida(String senha) {
        return senha != null && !senha.isBlank() && senha.length() >= 8
                && senha.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
    public static void exigir(String senha) {
        if (!valida(senha)) throw new IllegalArgumentException("A senha deve ter ao menos 8 caracteres e no máximo 72 bytes.");
    }
}
