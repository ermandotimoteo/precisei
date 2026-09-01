package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.precisei.dto.ProfissionalForm;
import com.precisei.service.ProfissionalService;
import com.precisei.service.ServicoService;
import com.precisei.service.AvaliacaoService;

import jakarta.validation.Valid;

@Controller
public class ProfissionalController {

    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    private final AvaliacaoService avaliacaoService;

    public ProfissionalController(ProfissionalService profissionalService,
            ServicoService servicoService, AvaliacaoService avaliacaoService) {
        this.profissionalService = profissionalService;
        this.servicoService = servicoService;
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/profissionais")
    public String listar(Model model) {
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("profissionalForm", new ProfissionalForm());
        adicionarAvaliacoes(model);
        return "profissionais";
    }

    @PostMapping("/profissionais/novo")
    public String cadastrar(
            @Valid @ModelAttribute("profissionalForm") ProfissionalForm form,
            BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        validar(form, bindingResult, true);
        if (bindingResult.hasErrors()) {
            prepararListagem(model, form);
            return "profissionais";
        }
        profissionalService.salvar(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Profissional cadastrado com sucesso.");
        return "redirect:/profissionais";
    }

    @GetMapping("/profissionais/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("profissionalForm", profissionalService.buscarFormulario(id));
        model.addAttribute("servicos", servicoService.listarTodos());
        return "profissional-form";
    }

    @PostMapping("/profissionais/{id}/editar")
    public String atualizar(@PathVariable Long id,
            @Valid @ModelAttribute("profissionalForm") ProfissionalForm form,
            BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        form.setId(id);
        validar(form, bindingResult, false);
        if (bindingResult.hasErrors()) {
            model.addAttribute("servicos", servicoService.listarTodos());
            return "profissional-form";
        }
        profissionalService.salvar(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Profissional atualizado com sucesso.");
        return "redirect:/profissionais";
    }

    private void validar(ProfissionalForm form, BindingResult bindingResult,
            boolean cadastro) {
        if (cadastro && (form.getSenha() == null || form.getSenha().isBlank()
                || form.getSenha().length() < 8)) {
            bindingResult.rejectValue("senha", "senha.invalida",
                    "Informe uma senha com pelo menos 8 caracteres.");
        } else if (!cadastro && form.getSenha() != null
                && !form.getSenha().isBlank() && form.getSenha().length() < 8) {
            bindingResult.rejectValue("senha", "senha.invalida",
                    "A nova senha deve possuir pelo menos 8 caracteres.");
        }
        if (!bindingResult.hasFieldErrors("email")
                && profissionalService.existeEmailDuplicado(form)) {
            bindingResult.rejectValue("email", "email.duplicado",
                    "Já existe um profissional com esse e-mail.");
        }
    }

    private void prepararListagem(Model model, ProfissionalForm form) {
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("profissionalForm", form);
        adicionarAvaliacoes(model);
    }

    private void adicionarAvaliacoes(Model model) {
        model.addAttribute("mediasAvaliacoes", avaliacaoService.mediasPorProfissional());
        model.addAttribute("quantidadesAvaliacoes", avaliacaoService.quantidadesPorProfissional());
    }
}
