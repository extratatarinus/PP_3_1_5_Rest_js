package ru.kata.spring.boot_security.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String startPage() {
        return "/auth/login";
    }

}
