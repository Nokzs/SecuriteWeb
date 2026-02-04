package com.example.walletback.dto;
import java.math.BigDecimal;

import com.example.walletback.entities.User;
public record UserDto(
    String email,      
    String role,      
    BigDecimal balance
) {
    public static UserDto fromEntity(User user) { // Ajout de 'static'
        return new UserDto(
            user.getEmail(),
            user.getRole(),
            user.getBalance()
        );
    }
}
