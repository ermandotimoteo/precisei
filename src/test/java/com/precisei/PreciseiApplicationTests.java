package com.precisei;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PreciseiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

    @Test
    void deveExibirPaginaInicialComCategorias() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("categorias"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Elétrica")));
    }

    @Test
    void deveExibirPaginaDeServicos() throws Exception {
        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos"))
                .andExpect(model().attributeExists("categorias", "servicos", "novaCategoria"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reparo de vazamento")));
    }

    @Test
    void deveExibirPaginaDeProfissionais() throws Exception {
        mockMvc.perform(get("/profissionais"))
                .andExpect(status().isOk())
                .andExpect(view().name("profissionais"))
                .andExpect(model().attributeExists("profissionais"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Carlos Oliveira")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Faxina residencial")));
    }

    @Test
    void deveRejeitarCadastroSemNome() throws Exception {
        mockMvc.perform(post("/servicos").param("nome", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("servicos"))
                .andExpect(model().attributeHasFieldErrors("novaCategoria", "nome"));
    }

    @Test
    void deveExibirPaginaDeClientes() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes"))
                .andExpect(model().attributeExists("clientes", "clienteForm"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Maria Souza")));
    }

    @Test
    void deveExibirPaginaDeSolicitacoes() throws Exception {
        mockMvc.perform(get("/solicitacoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("solicitacoes"))
                .andExpect(model().attributeExists("solicitacoes", "solicitacaoForm"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reparo de vazamento")));
    }

}
