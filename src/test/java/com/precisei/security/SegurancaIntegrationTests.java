package com.precisei.security;

import com.precisei.model.*;
import com.precisei.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SegurancaIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ClienteRepository clientes;
    @Autowired ProfissionalRepository profissionais;
    @Autowired ServicoRepository servicos;
    @Autowired CategoriaRepository categorias;
    @Autowired SolicitacaoServicoRepository pedidos;
    @Autowired PasswordEncoder encoder;
    Cliente titular, terceiro;
    Profissional prestador;
    Servico servico;
    SolicitacaoServico pedido;
    @BeforeEach void preparar() {
        String token = UUID.randomUUID().toString();
        titular = clientes.saveAndFlush(new Cliente("Titular", "000000000", token+"@example.invalid",encoder.encode("SenhaSegura123")));
        terceiro = clientes.saveAndFlush(new Cliente("Terceiro", "000000001", "b"+token+"@example.invalid",encoder.encode("SenhaSegura123")));
        servico = servicos.saveAndFlush(new Servico("Serviço " + token,"Atividade de teste",null,categorias.findAll().get(0)));
        prestador = profissionais.saveAndFlush(new Profissional("Prestador", "000000002","p"+token+"@example.invalid",encoder.encode("SenhaSegura123"), "Apresentação", "Cidade", "Bairro",true, Set.of(servico)));
        pedido = pedidos.saveAndFlush(new SolicitacaoServico(null,"Pedido particular " + token,null,titular,prestador,servico));
    }
    UsuarioAutenticado cliente(Cliente c) {return new UsuarioAutenticado(c.getId(),TipoConta.CLIENTE,c.getNome());}
    UsuarioAutenticado profissional() {return new UsuarioAutenticado(prestador.getId(),TipoConta.PROFISSIONAL,prestador.getNome());}
    @Test void anonimoNaoListaClientesNemPedidos() throws Exception {
        mvc.perform(get("/clientes")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/solicitacoes")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/cadastro/cliente")).andExpect(status().isOk()).andExpect(content().string(not(containsString(titular.getEmail()))));
    }
    @Test void cadastroSemCsrfNaoGrava() throws Exception {
        long count = clientes.count();
        mvc.perform(post("/clientes/novo").param("nome","Forjado")).andExpect(status().isForbidden());
        assertEquals(count,clientes.count());
    }
    @Test void clienteNaoEscreveCatalogo() throws Exception {
        mvc.perform(post("/servicos").with(user(cliente(titular))).with(csrf()).param("nome","Forjado")).andExpect(status().isForbidden());
    }
    @Test void soTitularVeSeuPedido() throws Exception {
        mvc.perform(get("/solicitacoes").with(user(cliente(terceiro)))).andExpect(status().isOk()).andExpect(content().string(not(containsString(pedido.getDescricao()))));
        mvc.perform(get("/solicitacoes").with(user(cliente(titular)))).andExpect(status().isOk()).andExpect(content().string(containsString(pedido.getDescricao())));
    }
    @Test void clienteNaoAceitaNemOutroClienteCancela() throws Exception {
        mvc.perform(post("/solicitacoes/{id}/status",pedido.getId()).with(user(cliente(titular))).with(csrf()).param("status","ACEITA")).andExpect(status().isForbidden());
        mvc.perform(post("/solicitacoes/{id}/status",pedido.getId()).with(user(cliente(terceiro))).with(csrf()).param("status","CANCELADA")).andExpect(status().isForbidden());
        assertEquals(StatusSolicitacao.PENDENTE,pedidos.findById(pedido.getId()).orElseThrow().getStatus());
    }
    @Test void profissionalAtribuidoAceita() throws Exception {
        mvc.perform(post("/solicitacoes/{id}/status",pedido.getId()).with(user(profissional())).with(csrf()).param("status","ACEITA")).andExpect(status().is3xxRedirection());
        assertEquals(StatusSolicitacao.ACEITA,pedidos.findById(pedido.getId()).orElseThrow().getStatus());
    }
    @Test void perfilAlheioNaoPodeSerEditado() throws Exception {
        mvc.perform(get("/profissionais/{id}/editar",prestador.getId()).with(user(cliente(titular)))).andExpect(status().isForbidden());
    }
    @Test void bindingRealRejeitaLimitesEInjecaoDeTitular() throws Exception {
        long count=pedidos.count();
        mvc.perform(post("/solicitacoes/novo").with(user(cliente(titular))).with(csrf())
                .param("clienteId",terceiro.getId().toString()).param("profissionalId",prestador.getId().toString())
                .param("servicoId",servico.getId().toString()).param("descricao","x".repeat(1001)))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("solicitacaoForm","descricao"))
                .andExpect(content().string(containsString("aria-describedby=\"sol-descricao-erro\"")));
        assertEquals(count,pedidos.count());
    }
    @Test void erroConcorrenteDeEmailTemRespostaSegura() throws Exception {
        mvc.perform(post("/clientes/novo").with(csrf()).param("nome","Novo").param("telefone","000000000")
                .param("email",titular.getEmail()).param("senha","SenhaSegura123"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("clienteForm","email"));
    }
    @Test void loginDistingueTipoERotacionaSessao() throws Exception {
        var sessao = new org.springframework.mock.web.MockHttpSession();
        String idAntes = sessao.getId();
        var resposta = mvc.perform(post("/login").session(sessao)
                .with(req -> { req.setRequestedSessionId(idAntes); req.setRequestedSessionIdValid(true); return req; })
                .with(csrf()).param("tipo","CLIENTE").param("username",titular.getEmail()).param("password","SenhaSegura123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/solicitacoes")).andReturn();
        assertNotEquals(idAntes, resposta.getRequest().getSession(false).getId());
        mvc.perform(post("/logout").session(sessao).with(csrf())).andExpect(redirectedUrl("/login?saiu"));
        assertTrue(sessao.isInvalid());
        mvc.perform(post("/login").with(csrf()).param("tipo","PROFISSIONAL").param("username",titular.getEmail()).param("password","SenhaSegura123"))
                .andExpect(redirectedUrl("/login?erro"));
    }
    @Test void naoExpoeActuatorEAplicaCabecalhos() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/login")).andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test void titularVemDaSessaoMesmoComFormularioForjado() throws Exception {
        String description = "Criação com titular forjado " + UUID.randomUUID();
        mvc.perform(post("/solicitacoes/novo").with(user(cliente(titular))).with(csrf())
                .param("clienteId", terceiro.getId().toString()).param("profissionalId", prestador.getId().toString())
                .param("servicoId", servico.getId().toString()).param("descricao", description))
                .andExpect(status().is3xxRedirection());
        var created = pedidos.findByClienteIdOrderByDataSolicitacaoDesc(titular.getId()).stream()
                .filter(p -> p.getDescricao().equals(description)).findFirst().orElseThrow();
        assertEquals(titular.getId(), created.getCliente().getId());
    }

    @Test void textoDoUsuarioEExibidoSemExecutarHtml() throws Exception {
        pedidos.saveAndFlush(new SolicitacaoServico(null, "<script>alert('teste')</script>", null, titular, prestador, servico));
        mvc.perform(get("/solicitacoes").with(user(cliente(titular))))
                .andExpect(status().isOk()).andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert('teste')</script>"))));
    }
}
