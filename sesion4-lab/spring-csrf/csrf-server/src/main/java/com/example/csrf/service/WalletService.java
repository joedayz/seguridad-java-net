package com.example.csrf.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Saldo simulado en sesion HTTP (como una sesion bancaria autenticada).
 */
@Service
@SessionScope
public class WalletService {

  private static final double INITIAL_BALANCE = 1000.0;

  private double balance = INITIAL_BALANCE;
  private String lastTransferMessage = "";

  public double getBalance() {
    return balance;
  }

  public String getLastTransferMessage() {
    return lastTransferMessage;
  }

  public void transfer(String to, double amount) {
    if (amount <= 0) {
      lastTransferMessage = "Importe invalido.";
      return;
    }
    if (amount > balance) {
      lastTransferMessage = "Saldo insuficiente.";
      return;
    }
    balance -= amount;
    lastTransferMessage = "Transferidos " + amount + " EUR a " + to + ".";
  }

  public void reset() {
    balance = INITIAL_BALANCE;
    lastTransferMessage = "";
  }
}
