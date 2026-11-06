package com.precisei;

import com.precisei.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcorrenciaIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ClienteRepository clientes;
    @Test void doisCadastrosSimultaneosGeramUmaContaSemErroInterno() throws Exception {
        String email = UUID.randomUUID()+"@example.invalid";
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barreira = new CyclicBarrier(2);
        Callable<Integer> cadastrar = () -> {
            barreira.await(10,TimeUnit.SECONDS);
            return mvc.perform(post("/clientes/novo").with(csrf()).param("nome","Concorrência")
                    .param("telefone","000000000").param("email",email).param("senha","SenhaConcorrente123"))
                    .andReturn().getResponse().getStatus();
        };
        try {
            var a=pool.submit(cadastrar);var b=pool.submit(cadastrar);
            int sa=a.get(30,TimeUnit.SECONDS),sb=b.get(30,TimeUnit.SECONDS);
            assertTrue(sa==302 || sb==302);
            assertTrue(sa<500 && sb<500);
            assertTrue(sa!=302 || sb!=302);
            assertTrue(clientes.findByEmailIgnoreCase(email).isPresent());
        } finally {
            pool.shutdownNow();
            clientes.findByEmailIgnoreCase(email).ifPresent(clientes::delete);
        }
    }
}
