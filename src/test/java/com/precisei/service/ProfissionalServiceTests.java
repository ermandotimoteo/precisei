package com.precisei.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.precisei.model.Profissional;
import com.precisei.repository.ProfissionalRepository;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTests {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    @Test
    void deveListarProfissionaisComSeusServicos() {
        List<Profissional> profissionais = List.of();
        when(profissionalRepository.findDistinctByOrderByNomeAsc()).thenReturn(profissionais);

        assertSame(profissionais, profissionalService.listarTodos());
    }
}
