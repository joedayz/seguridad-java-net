package com.example.xss.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

/**
 * DESPUES — SEGURO. Escapa la entrada antes de mostrarla y la vista usa {@code th:text},
 * que escapa HTML automaticamente.
 */
@Controller
@RequestMapping("/secure-comments")
public class SecureCommentController {

    @GetMapping
    public String showSecureForm(Model model) {
        model.addAttribute("comment", "");
        return "secure-comment-form";
    }

    @PostMapping
    public String submitSecureComment(@RequestParam String comment, Model model) {
        // SEGURO: escapamos en el servidor. Preferir th:text en la plantilla (escapa
        // automaticamente) o HtmlUtils.htmlEscape() si hay que escapar en el controlador.
        String safeComment = HtmlUtils.htmlEscape(comment);
        model.addAttribute("comment", safeComment);
        return "secure-comment-form";
    }
}
