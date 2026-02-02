package com.example.walletback.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/user")
public class UserController {

        @GetMapping
        public User getUser() {
            // Implementation to retrieve user information
        }

        @PostMapping("addMoney")
        public void addMoney(@RequestBody AddMoneyRequest request) {
            // Implementation to add money to user wallet
        }

        @PostMapping("transfer")
        public void transferMoney(@RequestBody TransferMoneyRequest request) {
            // Implementation to transfer money between users

        }
    }
