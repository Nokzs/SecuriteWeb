package com.example.securitewebback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.securitewebback.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.securitewebback.auth.entity.Proprietaire; // Import concret
import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.user.repository.ProprietaireRepository;
import com.example.securitewebback.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private ProprietaireRepository proprietaireRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("findByEmail : Retourne le propriétaire si trouvé")
    void findByEmail_Success() {
        Proprietaire p = new Proprietaire();
        p.setEmail("test@test.com");

        when(proprietaireRepository.findByEmail("test@test.com")).thenReturn(Optional.of(p));

        Proprietaire result = userService.findByEmail("test@test.com");

        assertThat(result).isEqualTo(p);
    }

    @Test
    @DisplayName("findByEmail : Exception si non trouvé")
    void findByEmail_NotFound() {
        when(proprietaireRepository.findByEmail("inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("inconnu"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("changePassword : Encode le mot de passe et retire le flag FirstLogin")
    void changePassword_Success() {
        User user = new Proprietaire();
        user.setIsFirstLogin(true);

        String newPass = "newSecret";
        String encodedPass = "encodedSecret";

        when(passwordEncoder.encode(newPass)).thenReturn(encodedPass);

        // On mock la sauvegarde pour retourner l'objet modifié
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User result = userService.changePassword(user, newPass);

        // Assert
        assertThat(result.getPassword()).isEqualTo(encodedPass);
        assertThat(result.getIsFirstLogin()).isFalse();
        verify(userRepository).save(user);
    }
}