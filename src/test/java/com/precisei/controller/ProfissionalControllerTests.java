package com.precisei.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.precisei.model.Categoria;
import com.precisei.service.CategoriaService;

@ExtendWith(MockitoExtension.class)
class ProfissionalControllerTests {

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ProfissionalController profissionalController;

    @Test
    void deveExibirProfissionaisComCategorias() {
        List<Categoria> categorias = List.of(new Categoria("Faxina"));
        when(categoriaService.listarTodas()).thenReturn(categorias);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = profissionalController.listar(model);

        assertEquals("profissionais", view);
        assertSame(categorias, model.get("categorias"));
    }
}
