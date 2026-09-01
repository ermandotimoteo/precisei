package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.precisei.dto.SolicitacaoForm;
import com.precisei.model.StatusSolicitacao;
import com.precisei.service.ClienteService;
import com.precisei.service.ProfissionalService;
import com.precisei.service.ServicoService;
import com.precisei.service.SolicitacaoService;

import jakarta.validation.Valid;

@Controller
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService,
            ClienteService clienteService, ProfissionalService profissionalService,
            ServicoService servicoService) {
        this.solicitacaoService = solicitacaoService;
        this.clienteService = clienteService;
        this.profissionalService = profissionalService;
        this.servicoService = servicoService;
    }

    @GetMapping("/solicitacoes")
    public String listar(Model model) {
        prepararPagina(model, new SolicitacaoForm());
        return "solicitacoes";
    }

    @PostMapping("/solicitacoes/novo")
    public String cadastrar(
            @Valid @ModelAttribute("solicitacaoForm") SolicitacaoForm form,
            BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasFieldErrors("profissionalId")
                && !bindingResult.hasFieldErrors("servicoId")
                && !solicitacaoService.profissionalPodeAtender(
                        form.getProfissionalId(), form.getServicoId())) {
            bindingResult.rejectValue("profissionalId", "profissional.incompativel",
                    "O profissional não está disponível para esse serviço.");
        }
        if (bindingResult.hasErrors()) {
            prepararPagina(model, form);
            return "solicitacoes";
        }
        solicitacaoService.cadastrar(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Solicitação cadastrada com sucesso.");
        return "redirect:/solicitacoes";
    }

    @PostMapping("/solicitacoes/{id}/status")
    public String alterarStatus(@PathVariable Long id,
            @RequestParam StatusSolicitacao status,
            RedirectAttributes redirectAttributes) {
        solicitacaoService.alterarStatus(id, status);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Status atualizado com sucesso.");
        return "redirect:/solicitacoes";
    }

    private void prepararPagina(Model model, SolicitacaoForm form) {
        model.addAttribute("solicitacoes", solicitacaoService.listarTodas());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("solicitacaoForm", form);
    }
}
