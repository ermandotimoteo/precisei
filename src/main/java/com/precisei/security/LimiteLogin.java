package com.precisei.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class LimiteLogin {
    private record Tentativas(int quantidade, Instant inicio) { }
    private final ConcurrentHashMap<String, Tentativas> tentativas = new ConcurrentHashMap<>();
    private final Clock clock;
    public LimiteLogin() { this(Clock.systemUTC()); }
    LimiteLogin(Clock clock) { this.clock = clock; }
    private boolean expirou(Tentativas t) { return t.inicio().plus(Duration.ofMinutes(15)).isBefore(clock.instant()); }
    public boolean bloqueado(String chave) {
        Tentativas t = tentativas.get(chave);
        return t != null && !expirou(t) && t.quantidade() >= (chave.startsWith("ip:") ? 50 : 10);
    }
    public void falhou(String chave) {
        // Limite de memória: falhar fechado até que a janela mais antiga expire.
        tentativas.entrySet().removeIf(e -> expirou(e.getValue()));
        if (tentativas.size() >= 4096 && !tentativas.containsKey(chave)) chave = "overflow";
        tentativas.compute(chave, (k, t) -> t == null || expirou(t)
                ? new Tentativas(1, clock.instant()) : new Tentativas(t.quantidade() + 1, t.inicio()));
    }
    public void sucesso(String chave) { tentativas.remove(chave); }
}
