package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.precisei.service.CategoriaService;

@Controller
public class ProfissionalController {

    private final CategoriaService categoriaService;

    public ProfissionalController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/profissionais")
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "profissionais";
    }
}
