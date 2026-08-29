package com.precisei.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.precisei.model.Categoria;
import com.precisei.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTests {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void deveListarCategoriasEmOrdemAlfabetica() {
        List<Categoria> categorias = List.of(new Categoria("Chaveiro"), new Categoria("Elétrica"));
        when(categoriaRepository.findAllByOrderByNomeAsc()).thenReturn(categorias);

        List<Categoria> resultado = categoriaService.listarTodas();

        assertSame(categorias, resultado);
    }

    @Test
    void deveRemoverEspacosDoNomeAntesDeCadastrar() {
        Categoria categoria = new Categoria("  Elétrica  ");
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaService.cadastrar(categoria);

        assertEquals("Elétrica", resultado.getNome());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void deveCadastrarSomenteCategoriasPadraoAusentes() {
        when(categoriaRepository.existsByNomeIgnoreCase("Elétrica")).thenReturn(true);
        when(categoriaRepository.existsByNomeIgnoreCase("Hidráulica")).thenReturn(false);

        categoriaService.garantirCategoriasPadrao(List.of("Elétrica", "Hidráulica"));

        verify(categoriaRepository, never()).save(argThat(categoria -> "Elétrica".equals(categoria.getNome())));
        verify(categoriaRepository).save(argThat(categoria -> "Hidráulica".equals(categoria.getNome())));
    }
}
