package com.example.walletback.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {
        private final JwtToUserConverter jwtToUserConverter;

        public SecurityConfig(JwtToUserConverter jwtToUserConverter) {
                this.jwtToUserConverter = jwtToUserConverter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authz -> authz
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(this.jwtToUserConverter)));

                return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                // 1. Récupération des clés (JWKS) via l'IP directe du VLAN 3
                // On évite ainsi les problèmes de DNS externe pour la validation technique
                String jwkSetUri = "http://192.168.3.3:8080/oauth2/jwks";
                NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

                // 2. Liste blanche des Issuers (Emetteurs) autorisés
                List<String> allowedIssuers = List.of(
                                "http://back.home.arpa",
                                "http://hotel.internal",
                                "http://auth.local:8080");

                // 3. Validateur personnalisé pour l'Issuer
                OAuth2TokenValidator<Jwt> issuerValidator = (jwt) -> {
                        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
                        if (allowedIssuers.contains(issuer)) {
                                return OAuth2TokenValidatorResult.success();
                        }
                        return OAuth2TokenValidatorResult.failure(
                                        new OAuth2Error("invalid_token", "Issuer non reconnu : " + issuer, null));
                };

                // 4. Combinaison avec les validateurs standards (expiration, etc.)
                OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
                                new JwtTimestampValidator(),
                                issuerValidator);

                jwtDecoder.setJwtValidator(combinedValidator);
                return jwtDecoder;
        }
}
