package ru.kata.spring.boot_security.controllers;


import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MainController {

    @GetMapping("/user")
    public String getUserInfo() {
        return "/user";
    }

    @GetMapping("/admin")
    public String getAdminPanel(Authentication auth) {
        return "/admin/admin_panel";
    }
}
