package com.precisei.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ErroAdvice {
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflito(Model model) {
        model.addAttribute("mensagem", "Este registro já existe ou está relacionado a outro cadastro. Revise os dados e tente novamente.");
        return "erro";
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalido(Model model) {
        model.addAttribute("mensagem", "Os dados informados não são válidos. Revise o formulário.");
        return "erro";
    }
    @ExceptionHandler(ResponseStatusException.class)
    public String regra(ResponseStatusException ex, Model model, jakarta.servlet.http.HttpServletResponse response) {
        response.setStatus(ex.getStatusCode().value());
        model.addAttribute("mensagem", ex.getReason());
        return "erro";
    }
}
