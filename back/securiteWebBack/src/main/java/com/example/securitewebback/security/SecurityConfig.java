package com.example.securitewebback.security;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ConcreteUserDetailsService myUserDetailsService;
    private final FormLoginSuccesHandler loginSuccessHandler;

    public SecurityConfig(ConcreteUserDetailsService myUserDetailsService,
                          FormLoginSuccesHandler loginSuccessHandler) {
        this.myUserDetailsService = myUserDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configuration du CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Configuration du CSRF (Double Cookie Submit Pattern)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/api/syndics/*/contact", "POST"),
                                new AntPathRequestMatcher("/api/auth/login", "POST")
                        ))

                // Filtre pour forcer l'envoi du cookie CSRF à chaque requête
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)

                // 3. Gestion des accès non autorisés (API Style : renvoie 401 au lieu d'une page de login)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // 4. Gestion de la session (Classique pour le mode Cookie/Session)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 5. Définition des règles de sécurité sur les URLs
                .authorizeHttpRequests(auth -> auth
                        // On ne met PAS /api/auth/login ici !
                        .requestMatchers("/api/auth/register", "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/syndics").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/syndics/*/contact").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())

                // 6. Configuration du Login (Form-data)
                .formLogin(form -> form
                        .usernameParameter("email")
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(this.loginSuccessHandler)
                        .failureHandler((req, res, exp) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"status\": \"ERROR\", \"message\": \"Identifiants invalides\"}");
                        }))

                // 7. Configuration du Logout
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .clearAuthentication(true));

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(myUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Autorise ton Front React
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Indispensable pour s'échanger les cookies (JSESSIONID / XSRF-TOKEN)
        configuration.setAllowCredentials(true);
        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-XSRF-TOKEN",
                "Accept",
                "X-Requested-With"
        ));
        // Headers exposés (pour que React puisse lire certains headers si besoin)
        configuration.setExposedHeaders(Collections.singletonList("X-XSRF-TOKEN"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}