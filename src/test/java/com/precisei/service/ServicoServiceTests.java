package com.precisei.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.precisei.model.Servico;
import com.precisei.repository.ServicoRepository;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTests {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    @Test
    void deveListarServicosEmOrdemAlfabetica() {
        List<Servico> servicos = List.of();
        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(servicos);

        assertSame(servicos, servicoService.listarTodos());
    }
}
