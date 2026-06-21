package com.splitease.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/login", "/app"})
    public String index(Model model) {
        model.addAttribute("appName", "SplitEase");
        model.addAttribute("appVersion", "1.0");
        return "index";
    }
}
