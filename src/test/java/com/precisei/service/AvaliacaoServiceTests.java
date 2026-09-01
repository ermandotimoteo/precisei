package com.precisei.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.precisei.dto.AvaliacaoForm;
import com.precisei.model.Avaliacao;
import com.precisei.model.SolicitacaoServico;
import com.precisei.model.StatusSolicitacao;
import com.precisei.repository.AvaliacaoRepository;
import com.precisei.repository.SolicitacaoServicoRepository;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTests {

    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private SolicitacaoServicoRepository solicitacaoRepository;
    @Mock private SolicitacaoServico solicitacao;
    @InjectMocks private AvaliacaoService service;

    @Test
    void deveCadastrarAvaliacaoParaSolicitacaoConcluida() {
        AvaliacaoForm form = formulario();
        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(solicitacao.getStatus()).thenReturn(StatusSolicitacao.CONCLUIDA);
        when(avaliacaoRepository.existsBySolicitacaoId(1L)).thenReturn(false);
        when(avaliacaoRepository.save(org.mockito.ArgumentMatchers.any(Avaliacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Avaliacao avaliacao = service.cadastrar(1L, form);

        assertEquals(5, avaliacao.getNota());
        assertEquals("Excelente atendimento", avaliacao.getComentario());
        verify(avaliacaoRepository).save(org.mockito.ArgumentMatchers.any(Avaliacao.class));
    }

    @Test
    void deveRejeitarSolicitacaoNaoConcluida() {
        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(solicitacao.getStatus()).thenReturn(StatusSolicitacao.ACEITA);

        assertThrows(ResponseStatusException.class,
                () -> service.cadastrar(1L, formulario()));
        verify(avaliacaoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveRejeitarSegundaAvaliacao() {
        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(solicitacao.getStatus()).thenReturn(StatusSolicitacao.CONCLUIDA);
        when(avaliacaoRepository.existsBySolicitacaoId(1L)).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> service.cadastrar(1L, formulario()));
        verify(avaliacaoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private AvaliacaoForm formulario() {
        AvaliacaoForm form = new AvaliacaoForm();
        form.setNota((byte) 5);
        form.setComentario(" Excelente atendimento ");
        return form;
    }
}
