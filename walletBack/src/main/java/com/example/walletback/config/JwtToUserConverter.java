package com.example.walletback.config;

import com.example.walletback.entities.User;
import com.example.walletback.repository.UserRepository;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class JwtToUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;
    private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public JwtToUserConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        System.out.println("🔍 [DEBUG APP B] Début conversion JWT. Subject: " + jwt.getSubject());

        try {
            String subject = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String role = jwt.getClaimAsString("role");

            UUID ssoID;
            try {
                ssoID = UUID.fromString(subject);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ [DEBUG APP B] Le subject '" + subject + "' n'est pas un UUID !");
                throw e;
            }

            User user = userRepository.findBySsoId(ssoID)
                    .orElseGet(() -> userRepository.findByEmail(email)
                            .orElseGet(() -> {
                                System.out.println("🆕 [DEBUG APP B] Création user: " + email);
                                return userRepository.save(User.builder()
                                        .ssoId(ssoID)
                                        .email(email)
                                        .role(role)
                                        .balance(BigDecimal.ZERO)
                                        .currency("EUR")
                                        .build());
                            }));

            System.out.println("✅ [DEBUG APP B] Authentification réussie pour: " + email);
            return new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt), subject);

        } catch (Exception e) {
            System.err.println("💥 [DEBUG APP B] Erreur fatale convertisseur: " + e.getMessage());
            e.printStackTrace();
            return null; // Spring Security traitera cela comme une erreur d'auth
        }
    }
}
