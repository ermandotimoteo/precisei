package com.precisei.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.precisei.dto.SolicitacaoForm;
import com.precisei.model.StatusSolicitacao;
import com.precisei.service.ClienteService;
import com.precisei.service.ProfissionalService;
import com.precisei.service.ServicoService;
import com.precisei.service.SolicitacaoService;

@ExtendWith(MockitoExtension.class)
class SolicitacaoControllerTests {

    @Mock private SolicitacaoService solicitacaoService;
    @Mock private ClienteService clienteService;
    @Mock private ProfissionalService profissionalService;
    @Mock private ServicoService servicoService;
    @InjectMocks private SolicitacaoController controller;

    @Test
    void deveExibirSolicitacoesEFormulario() {
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("solicitacoes", controller.listar(model));
        assertTrue(model.containsAttribute("solicitacoes"));
        assertTrue(model.get("solicitacaoForm") instanceof SolicitacaoForm);
    }

    @Test
    void deveRejeitarProfissionalIncompativel() {
        SolicitacaoForm form = criarForm();
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(form, "solicitacaoForm");
        when(solicitacaoService.profissionalPodeAtender(2L, 3L)).thenReturn(false);

        assertEquals("solicitacoes", controller.cadastrar(form, result,
                new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertTrue(result.hasFieldErrors("profissionalId"));
        verify(solicitacaoService, never()).cadastrar(form);
    }

    @Test
    void deveCadastrarSolicitacaoCompativel() {
        SolicitacaoForm form = criarForm();
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(form, "solicitacaoForm");
        when(solicitacaoService.profissionalPodeAtender(2L, 3L)).thenReturn(true);

        assertEquals("redirect:/solicitacoes", controller.cadastrar(form, result,
                new ExtendedModelMap(), new RedirectAttributesModelMap()));
        verify(solicitacaoService).cadastrar(form);
    }

    @Test
    void deveAtualizarStatus() {
        assertEquals("redirect:/solicitacoes", controller.alterarStatus(
                1L, StatusSolicitacao.ACEITA, new RedirectAttributesModelMap()));
        verify(solicitacaoService).alterarStatus(1L, StatusSolicitacao.ACEITA);
    }

    private SolicitacaoForm criarForm() {
        SolicitacaoForm form = new SolicitacaoForm();
        form.setClienteId(1L);
        form.setProfissionalId(2L);
        form.setServicoId(3L);
        form.setDescricao("Atendimento de teste");
        return form;
    }
}
