package com.precisei.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.precisei.model.Servico;
import com.precisei.model.Categoria;
import com.precisei.dto.ServicoForm;
import com.precisei.repository.ServicoRepository;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTests {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ServicoService servicoService;

    @Test
    void deveListarServicosEmOrdemAlfabetica() {
        List<Servico> servicos = List.of();
        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(servicos);

        assertSame(servicos, servicoService.listarTodos());
    }

    @Test
    void deveCadastrarServicoRelacionadoACategoria() {
        ServicoForm form = new ServicoForm();
        form.setNome("Troca de fechadura");
        form.setDescricao("Troca residencial.");
        form.setCategoriaId(1L);
        Categoria categoria = new Categoria("Chaveiro");
        when(categoriaService.buscarPorId(1L)).thenReturn(categoria);

        servicoService.salvar(form);

        verify(servicoRepository).save(any(Servico.class));
    }
}
