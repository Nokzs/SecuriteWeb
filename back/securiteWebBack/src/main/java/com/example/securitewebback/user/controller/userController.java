package com.example.securitewebback.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.security.CustomUserDetails;
import com.example.securitewebback.user.dto.UserDto;
import com.example.securitewebback.user.service.UserService;
import com.example.securitewebback.user.dto.UserLookupDto;
import com.example.securitewebback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class userController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/by-email")
    public ResponseEntity<Proprietaire> getUserById(@RequestParam("email") String email) {
        Proprietaire user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public UserDto getProfil(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        User connectedUser = userService.findByEmail(user.getEmail());
        return UserDto.fromEntity(connectedUser);
    }

    @GetMapping("/lookup")
    public ResponseEntity<UserLookupDto> lookupByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(new UserLookupDto(user.getId(), user.getEmail())))
                .orElse(ResponseEntity.notFound().build());
    }
}
