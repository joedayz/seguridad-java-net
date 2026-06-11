package com.example.csrf.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Pagina que simula un sitio malicioso (evil.com) con un formulario oculto que
 * intenta transferir dinero en nombre del usuario.
 */
@Controller
public class AttackerController {

  @GetMapping("/attacker")
  public String attackerPage() {
    return "attacker";
  }
}
