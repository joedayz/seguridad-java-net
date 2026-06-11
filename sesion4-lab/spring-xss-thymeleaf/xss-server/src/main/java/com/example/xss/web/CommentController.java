package com.example.xss.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ANTES — VULNERABLE. Pasa la entrada del usuario al modelo sin escapar y la vista
 * la renderiza con {@code th:utext}, que no escapa HTML.
 */
@Controller
@RequestMapping("/comments")
public class CommentController {

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("comment", "");
        return "comment-form";
    }

    @PostMapping
    public String submitComment(@RequestParam String comment, Model model) {
        // PELIGRO: la entrada del usuario va "tal cual" al modelo. Si alguien envia
        // <script>alert('XSS')</script>, la vista lo ejecutara en el navegador.
        model.addAttribute("comment", comment);
        return "comment-form";
    }
}
