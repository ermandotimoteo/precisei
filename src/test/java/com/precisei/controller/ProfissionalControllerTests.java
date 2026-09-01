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

import com.precisei.model.Profissional;
import com.precisei.service.ProfissionalService;

@ExtendWith(MockitoExtension.class)
class ProfissionalControllerTests {

    @Mock
    private ProfissionalService profissionalService;

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
}
