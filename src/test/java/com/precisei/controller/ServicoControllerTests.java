package com.precisei.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.precisei.model.Categoria;
import com.precisei.dto.ServicoForm;
import com.precisei.service.CategoriaService;
import com.precisei.service.ServicoService;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTests {

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ServicoController servicoController;

    @Test
    void deveExibirCategoriasEFormulario() {
        List<Categoria> categorias = List.of(new Categoria("Elétrica"));
        when(categoriaService.listarTodas()).thenReturn(categorias);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = servicoController.listar(model);

        assertEquals("servicos", view);
        assertEquals(categorias, model.get("categorias"));
        assertEquals(List.of(), model.get("servicos"));
        assertTrue(model.get("novaCategoria") instanceof Categoria);
    }

    @Test
    void deveRejeitarNomeDeCategoriaDuplicado() {
        Categoria categoria = new Categoria("Elétrica");
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(categoria, "novaCategoria");
        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        when(categoriaService.existeComNome("Elétrica")).thenReturn(true);
        when(categoriaService.listarTodas()).thenReturn(List.of());

        String view = servicoController.cadastrar(
                categoria, bindingResult, model, redirectAttributes);

        assertEquals("servicos", view);
        assertTrue(bindingResult.hasFieldErrors("nome"));
        verify(categoriaService, never()).cadastrar(categoria);
    }

    @Test
    void deveCadastrarCategoriaValida() {
        Categoria categoria = new Categoria("Pintura");
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(categoria, "novaCategoria");
        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = servicoController.cadastrar(
                categoria, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/servicos", view);
        assertEquals("Categoria cadastrada com sucesso.",
                redirectAttributes.getFlashAttributes().get("mensagemSucesso"));
        verify(categoriaService).cadastrar(categoria);
    }

    @Test
    void deveCadastrarServicoValido() {
        ServicoForm form = criarServicoForm();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(form, "servicoForm");

        String view = servicoController.cadastrarServico(
                form, bindingResult, new ExtendedModelMap(),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/servicos", view);
        verify(servicoService).salvar(form);
    }

    @Test
    void deveRejeitarServicoDuplicadoNaCategoria() {
        ServicoForm form = criarServicoForm();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(form, "servicoForm");
        when(servicoService.existeDuplicado(form)).thenReturn(true);

        String view = servicoController.cadastrarServico(
                form, bindingResult, new ExtendedModelMap(),
                new RedirectAttributesModelMap());

        assertEquals("servicos", view);
        assertTrue(bindingResult.hasFieldErrors("nome"));
        verify(servicoService, never()).salvar(form);
    }

    private ServicoForm criarServicoForm() {
        ServicoForm form = new ServicoForm();
        form.setNome("Troca de fechadura");
        form.setDescricao("Troca de fechadura residencial.");
        form.setCategoriaId(1L);
        return form;
    }
}
