package com.precisei.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.precisei.dto.ProfissionalForm;
import com.precisei.model.Profissional;
import com.precisei.service.ProfissionalService;
import com.precisei.service.ServicoService;

@ExtendWith(MockitoExtension.class)
class ProfissionalControllerTests {

    @Mock
    private ProfissionalService profissionalService;

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ProfissionalController profissionalController;

    @Test
    void deveExibirProfissionaisDoBanco() {
        List<Profissional> profissionais = List.of();
        when(profissionalService.listarTodos()).thenReturn(profissionais);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = profissionalController.listar(model);

        assertEquals("profissionais", view);
        assertSame(profissionais, model.get("profissionais"));
    }

    @Test
    void deveExigirSenhaSeguraNoCadastro() {
        ProfissionalForm form = criarProfissionalForm();
        form.setSenha("123");
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(form, "profissionalForm");

        String view = profissionalController.cadastrar(form, bindingResult,
                new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertEquals("profissionais", view);
        assertTrue(bindingResult.hasFieldErrors("senha"));
        verify(profissionalService, never()).salvar(form);
    }

    @Test
    void deveCadastrarProfissionalValido() {
        ProfissionalForm form = criarProfissionalForm();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(form, "profissionalForm");

        String view = profissionalController.cadastrar(form, bindingResult,
                new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertEquals("redirect:/profissionais", view);
        verify(profissionalService).salvar(form);
    }

    private ProfissionalForm criarProfissionalForm() {
        ProfissionalForm form = new ProfissionalForm();
        form.setNome("Profissional Teste");
        form.setEmail("teste@precisei.local");
        form.setTelefone("(83) 90000-0000");
        form.setSenha("senha-segura");
        form.setCidade("João Pessoa");
        form.setBairro("Centro");
        form.setServicoIds(List.of(1L));
        return form;
    }
}
