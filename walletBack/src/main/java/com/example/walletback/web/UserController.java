package com.example.walletback.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;

import com.example.walletback.entities.User;
import com.example.walletback.dto.AddMoneyRequest;
import com.example.walletback.dto.TransfertMoneyRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping
    public User getUser() {
        // En attendant ton service, tu peux retourner null pour tester la compilation
        return null;
    }

    @PostMapping("/addMoney") // Le "/" est préférable
    public void addMoney(@RequestBody AddMoneyRequest request, Authentication authentication) {
        // Logique ici
    }

    @PostMapping("/transfer")
    public void transferMoney(@RequestBody TransfertMoneyRequest request, Authentication authentication) {
        // Logique ici
    }
}
