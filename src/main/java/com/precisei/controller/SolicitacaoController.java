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
import com.precisei.dto.AvaliacaoForm;
import com.precisei.model.StatusSolicitacao;
import com.precisei.service.ClienteService;
import com.precisei.service.ProfissionalService;
import com.precisei.service.ServicoService;
import com.precisei.service.SolicitacaoService;
import com.precisei.service.AvaliacaoService;

import jakarta.validation.Valid;

@Controller
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    private final AvaliacaoService avaliacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService,
            ClienteService clienteService, ProfissionalService profissionalService,
            ServicoService servicoService, AvaliacaoService avaliacaoService) {
        this.solicitacaoService = solicitacaoService;
        this.clienteService = clienteService;
        this.profissionalService = profissionalService;
        this.servicoService = servicoService;
        this.avaliacaoService = avaliacaoService;
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

    @PostMapping("/solicitacoes/{id}/avaliacao")
    public String avaliar(@PathVariable Long id,
            @Valid @ModelAttribute("avaliacaoForm") AvaliacaoForm form,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Escolha uma nota entre 1 e 5 e revise o comentário.");
            return "redirect:/solicitacoes";
        }
        try {
            avaliacaoService.cadastrar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Avaliação registrada com sucesso.");
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getReason());
        }
        return "redirect:/solicitacoes";
    }

    private void prepararPagina(Model model, SolicitacaoForm form) {
        model.addAttribute("solicitacoes", solicitacaoService.listarTodas());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("solicitacaoForm", form);
        model.addAttribute("avaliacaoForm", new AvaliacaoForm());
    }
}
