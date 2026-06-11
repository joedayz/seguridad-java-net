package com.example.csrf.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.csrf.service.WalletService;

/**
 * DESPUES — SEGURO. Misma transferencia con CSRF habilitado: el formulario debe
 * incluir el token sincronizador generado por Spring Security.
 */
@Controller
@RequestMapping("/secure")
public class SecureTransferController {

  private final WalletService wallet;

  public SecureTransferController(WalletService wallet) {
    this.wallet = wallet;
  }

  @GetMapping
  public String showForm(Model model) {
    model.addAttribute("balance", wallet.getBalance());
    model.addAttribute("message", wallet.getLastTransferMessage());
    return "secure/transfer";
  }

  @PostMapping("/transfer")
  public String transfer(
      @RequestParam String to,
      @RequestParam double amount,
      Model model) {
    wallet.transfer(to, amount);
    model.addAttribute("balance", wallet.getBalance());
    model.addAttribute("message", wallet.getLastTransferMessage());
    return "secure/transfer";
  }

  @PostMapping("/reset")
  public String reset() {
    wallet.reset();
    return "redirect:/secure";
  }
}
