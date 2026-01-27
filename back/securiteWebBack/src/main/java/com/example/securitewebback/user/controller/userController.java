package com.example.securitewebback.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.user.service.UserService;

@RestController
@RequestMapping("/api/user")
public class userController {
    @Autowired
    private UserService userService;

    @GetMapping("/by-email")
    public ResponseEntity<Proprietaire> getUserById(@RequestParam("email") String email) {
        Proprietaire user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }

}
