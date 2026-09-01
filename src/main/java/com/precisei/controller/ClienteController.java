package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.precisei.dto.ClienteForm;
import com.precisei.service.ClienteService;

import jakarta.validation.Valid;

@Controller
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/clientes")
    public String listar(Model model) {
        prepararPagina(model, new ClienteForm());
        return "clientes";
    }

    @PostMapping("/clientes/novo")
    public String cadastrar(@Valid @ModelAttribute("clienteForm") ClienteForm form,
            BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        if (form.getSenha() == null || form.getSenha().isBlank()
                || form.getSenha().length() < 8) {
            bindingResult.rejectValue("senha", "senha.invalida",
                    "Informe uma senha com pelo menos 8 caracteres.");
        }
        if (!bindingResult.hasFieldErrors("email")
                && clienteService.existeEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "email.duplicado",
                    "Já existe um cliente com esse e-mail.");
        }
        if (bindingResult.hasErrors()) {
            prepararPagina(model, form);
            return "clientes";
        }
        clienteService.cadastrar(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Cliente cadastrado com sucesso.");
        return "redirect:/clientes";
    }

    private void prepararPagina(Model model, ClienteForm form) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("clienteForm", form);
    }
}
