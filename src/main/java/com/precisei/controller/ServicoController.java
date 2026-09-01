package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.precisei.model.Categoria;
import com.precisei.service.CategoriaService;
import com.precisei.service.ServicoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/servicos")
public class ServicoController {

    private final CategoriaService categoriaService;
    private final ServicoService servicoService;

    public ServicoController(CategoriaService categoriaService, ServicoService servicoService) {
        this.categoriaService = categoriaService;
        this.servicoService = servicoService;
    }

    @GetMapping
    public String listar(Model model) {
        prepararPagina(model, new Categoria(""));
        return "servicos";
    }

    @PostMapping
    public String cadastrar(
            @Valid @ModelAttribute("novaCategoria") Categoria novaCategoria,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!bindingResult.hasFieldErrors("nome")
                && categoriaService.existeComNome(novaCategoria.getNome())) {
            bindingResult.rejectValue("nome", "categoria.duplicada",
                    "Já existe uma categoria com esse nome.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("servicos", servicoService.listarTodos());
            return "servicos";
        }

        categoriaService.cadastrar(novaCategoria);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria cadastrada com sucesso.");
        return "redirect:/servicos";
    }

    private void prepararPagina(Model model, Categoria novaCategoria) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("novaCategoria", novaCategoria);
    }
}
