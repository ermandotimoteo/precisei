package com.precisei.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.precisei.security.UsuarioAutenticado;

@ControllerAdvice
public class InterfaceAdvice {
    @ModelAttribute("usuarioAtual")
    public UsuarioAutenticado usuario(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticado user ? user : null;
    }
    @ModelAttribute("manutencao")
    public boolean manutencao(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANUTENCAO"));
    }
}
