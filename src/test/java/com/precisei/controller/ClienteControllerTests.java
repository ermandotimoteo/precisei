package com.precisei.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.precisei.dto.ClienteForm;
import com.precisei.service.ClienteService;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTests {

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController controller;

    @Test
    void deveExibirClientesEFormulario() {
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("clientes", controller.listar(model));
        assertTrue(model.containsAttribute("clientes"));
        assertTrue(model.get("clienteForm") instanceof ClienteForm);
    }

    @Test
    void deveRejeitarSenhaCurta() {
        ClienteForm form = criarForm();
        form.setSenha("123");
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(form, "clienteForm");

        assertEquals("clientes", controller.cadastrar(form, result,
                new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertTrue(result.hasFieldErrors("senha"));
        verify(clienteService, never()).cadastrar(form);
    }

    @Test
    void deveCadastrarClienteValido() {
        ClienteForm form = criarForm();
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(form, "clienteForm");

        assertEquals("redirect:/clientes", controller.cadastrar(form, result,
                new ExtendedModelMap(), new RedirectAttributesModelMap()));
        verify(clienteService).cadastrar(form);
    }

    private ClienteForm criarForm() {
        ClienteForm form = new ClienteForm();
        form.setNome("Cliente Teste");
        form.setTelefone("(83) 90000-0000");
        form.setEmail("cliente@teste.local");
        form.setSenha("senha-segura");
        return form;
    }
}
