package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.precisei.service.CategoriaService;

@Controller
public class HomeController {

    private final CategoriaService categoriaService;

    public HomeController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "index";
    }
}
