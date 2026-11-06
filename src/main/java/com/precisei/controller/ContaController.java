package com.precisei.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContaController {
    @GetMapping("/login") public String login() { return "login"; }
    @GetMapping("/privacidade") public String privacidade() { return "privacidade"; }
}
