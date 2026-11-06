package com.precisei.tools;

import java.util.Arrays;
import com.precisei.config.SenhaConfig;
import com.precisei.security.PoliticaSenha;

/** Utilitário local; nunca recebe senhas por argumento nem por endpoint web. */
public final class SenhaTool {
    private SenhaTool() { }
    public static void main(String[] args) {
        var encoder = SenhaConfig.criarEncoder(12);
        if (args.length == 1 && args[0].equals("--benchmark")) {
            String sample = java.util.UUID.randomUUID().toString();
            for (int i=0;i<3;i++) {
                long start=System.nanoTime();String hash=encoder.encode(sample);
                if (!encoder.matches(sample,hash)) throw new IllegalStateException("Verificação falhou");
                System.out.printf("argon2id encode+verify sample=%d milliseconds=%.1f%n",i+1,(System.nanoTime()-start)/1_000_000d);
            }
            return;
        }
        var console = System.console();
        if (console == null) throw new IllegalStateException("Execute em um terminal interativo para proteger a entrada da senha.");
        char[] senha = console.readPassword("Senha para a conta de manutenção: ");
        try { PoliticaSenha.exigir(new String(senha)); console.printf("%s%n",encoder.encode(new String(senha))); }
        finally { Arrays.fill(senha,'\0'); }
    }
}
