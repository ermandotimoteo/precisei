package com.precisei.security;

import com.precisei.config.SenhaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class EncoderSenhaTests {
    @Test void preservaBcryptLegadoEPropõeAtualizacao() {
        var encoder=SenhaConfig.criarEncoder(4);
        String legado=new BCryptPasswordEncoder(4).encode("SenhaSegura123");
        assertTrue(encoder.matches("SenhaSegura123",legado));
        assertTrue(encoder.upgradeEncoding(legado));
        String novo=encoder.encode("SenhaSegura123");
        assertTrue(novo.startsWith("{argon2id}"));
        assertTrue(encoder.matches("SenhaSegura123",novo));
        assertFalse(encoder.matches("OutraSenha123",novo));
        assertFalse(encoder.upgradeEncoding(novo));
    }
}
