package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.precisei.model.Categoria;
import com.precisei.dto.ServicoForm;
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
        prepararPagina(model, new Categoria(""), new ServicoForm());
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
            prepararPagina(model, novaCategoria, new ServicoForm());
            return "servicos";
        }

        categoriaService.cadastrar(novaCategoria);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria cadastrada com sucesso.");
        return "redirect:/servicos";
    }

    @PostMapping("/novo")
    public String cadastrarServico(
            @Valid @ModelAttribute("servicoForm") ServicoForm form,
            BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        validarServicoDuplicado(form, bindingResult);
        if (bindingResult.hasErrors()) {
            prepararPagina(model, new Categoria(""), form);
            return "servicos";
        }
        servicoService.salvar(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Serviço cadastrado com sucesso.");
        return "redirect:/servicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("servicoForm", servicoService.buscarFormulario(id));
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "servico-form";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
            @Valid @ModelAttribute("servicoForm") ServicoForm form,
            BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        form.setId(id);
        validarServicoDuplicado(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listarTodas());
            return "servico-form";
        }
        servicoService.salvar(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Serviço atualizado com sucesso.");
        return "redirect:/servicos";
    }

    private void validarServicoDuplicado(ServicoForm form, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("nome")
                && !bindingResult.hasFieldErrors("categoriaId")
                && servicoService.existeDuplicado(form)) {
            bindingResult.rejectValue("nome", "servico.duplicado",
                    "Já existe um serviço com esse nome na categoria selecionada.");
        }
    }

    private void prepararPagina(Model model, Categoria novaCategoria, ServicoForm form) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("novaCategoria", novaCategoria);
        model.addAttribute("servicoForm", form);
    }
}
