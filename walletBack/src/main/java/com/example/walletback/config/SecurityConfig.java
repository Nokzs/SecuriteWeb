package com.example.walletback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
}
