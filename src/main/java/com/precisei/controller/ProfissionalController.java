package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.precisei.service.ProfissionalService;

@Controller
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    public ProfissionalController(ProfissionalService profissionalService) {
        this.profissionalService = profissionalService;
    }

    @GetMapping("/profissionais")
    public String listar(Model model) {
        model.addAttribute("profissionais", profissionalService.listarTodos());
        return "profissionais";
    }
}
