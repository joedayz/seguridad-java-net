package com.example.csrf.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.csrf.service.WalletService;

/**
 * ANTES — VULNERABLE. Transferencia sin token CSRF (cadena de seguridad con csrf
 * deshabilitado en {@code /vulnerable/**}).
 */
@Controller
@RequestMapping("/vulnerable")
public class VulnerableTransferController {

  private final WalletService wallet;

  public VulnerableTransferController(WalletService wallet) {
    this.wallet = wallet;
  }

  @GetMapping
  public String showForm(Model model) {
    model.addAttribute("balance", wallet.getBalance());
    model.addAttribute("message", wallet.getLastTransferMessage());
    return "vulnerable/transfer";
  }

  @PostMapping("/transfer")
  public String transfer(
      @RequestParam String to,
      @RequestParam double amount,
      Model model) {
    // PELIGRO: cualquier sitio puede forzar este POST si el usuario tiene sesion activa
    // y CSRF esta deshabilitado.
    wallet.transfer(to, amount);
    model.addAttribute("balance", wallet.getBalance());
    model.addAttribute("message", wallet.getLastTransferMessage());
    return "vulnerable/transfer";
  }

  @PostMapping("/reset")
  public String reset() {
    wallet.reset();
    return "redirect:/vulnerable";
  }
}
