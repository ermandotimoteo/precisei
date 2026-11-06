package com.precisei;

import com.precisei.model.*;
import com.precisei.repository.*;
import com.precisei.security.*;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AtendimentoConcorrenteTests {
    @Autowired MockMvc mvc;
    @Autowired ClienteRepository clientes;
    @Autowired ProfissionalRepository profissionais;
    @Autowired ServicoRepository servicos;
    @Autowired CategoriaRepository categorias;
    @Autowired SolicitacaoServicoRepository pedidos;
    @Autowired AvaliacaoRepository avaliacoes;
    @Autowired TransactionTemplate transaction;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Test void duasAceitacoesAplicamUmaUnicaTransicao() throws Exception {
        executar(false);
    }

    @Test void duasAvaliacoesSimultaneasGeramUmUnicoRegistro() throws Exception {
        executar(true);
    }

    private void executar(boolean avaliar) throws Exception {
        SolicitacaoServico pedido = transaction.execute(status -> {
            String token = UUID.randomUUID().toString();
            var c = clientes.saveAndFlush(new Cliente("Cliente concorrente", "000000000", token+"@example.invalid", "hash-test"));
            var s = servicos.saveAndFlush(new Servico("Concorrência " + token, "Teste isolado", null, categorias.findAll().get(0)));
            var p = profissionais.saveAndFlush(new Profissional("Prestador concorrente", "000000001", "p"+token+"@example.invalid", "hash-test", null, "Cidade", "Bairro", true, Set.of(s)));
            var novo = new SolicitacaoServico(null, "Teste de disputa " + token, null, c, p, s);
            if (avaliar) {
                novo.alterarStatus(StatusSolicitacao.ACEITA);
                novo.alterarStatus(StatusSolicitacao.EM_ANDAMENTO);
                novo.alterarStatus(StatusSolicitacao.CONCLUIDA);
            }
            return pedidos.saveAndFlush(novo);
        });
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CyclicBarrier barrier = new CyclicBarrier(2);
            var principal = avaliar
                    ? new UsuarioAutenticado(pedido.getCliente().getId(), TipoConta.CLIENTE, "Cliente")
                    : new UsuarioAutenticado(pedido.getProfissional().getId(), TipoConta.PROFISSIONAL, "Prestador");
            Callable<org.springframework.test.web.servlet.MvcResult> action = () -> {
                barrier.await(10, TimeUnit.SECONDS);
                var request = post("/solicitacoes/{id}/" + (avaliar ? "avaliacao" : "status"), pedido.getId())
                        .with(user(principal)).with(csrf());
                if (avaliar) request.param("nota", "5"); else request.param("status", "ACEITA");
                return mvc.perform(request).andReturn();
            };
            var first = pool.submit(action); var second = pool.submit(action);
            var a = first.get(30, TimeUnit.SECONDS); var b = second.get(30, TimeUnit.SECONDS);
            assertTrue(a.getResponse().getStatus() < 500 && b.getResponse().getStatus() < 500);
            if (avaliar) {
                assertTrue(avaliacoes.existsBySolicitacaoId(pedido.getId()));
                long successes = java.util.stream.Stream.of(a, b)
                        .filter(r -> r.getFlashMap().containsKey("mensagemSucesso")).count();
                assertEquals(1, successes);
                assertEquals(1, java.util.stream.Stream.of(a, b)
                        .filter(r -> r.getFlashMap().containsKey("mensagemErro")).count());
            } else {
                assertEquals(StatusSolicitacao.ACEITA, pedidos.findById(pedido.getId()).orElseThrow().getStatus());
                assertEquals(1, java.util.stream.Stream.of(a,b).filter(r -> r.getResponse().getStatus()==302).count());
                assertEquals(1, java.util.stream.Stream.of(a,b).filter(r -> r.getResponse().getStatus()==403).count());
            }
        } finally {
            pool.shutdownNow();
            transaction.executeWithoutResult(status -> {
                // Exclusão restrita à fixture; não deixa referência JPA carregada para entidade removida.
                jdbc.update("delete from avaliacoes where id_solicitacao=?", pedido.getId());
                jdbc.update("delete from solicitacoes_servico where id_solicitacao=?", pedido.getId());
                jdbc.update("delete from profissionais where id_profissional=?", pedido.getProfissional().getId());
                jdbc.update("delete from servicos where id_servico=?", pedido.getServico().getId());
                jdbc.update("delete from clientes where id_cliente=?", pedido.getCliente().getId());
            });
        }
    }
}
