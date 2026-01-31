package com.example.securitewebback.security;

import java.util.Arrays;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.securitewebback.security.SsoConfig.CustomTokenSuccessHandler;

import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final ConcreteUserDetailsService myUserDetailsService;

        public SecurityConfig(ConcreteUserDetailsService myUserDetailsService,
                        FormLoginSuccesHandler loginSuccessHandler) {
                this.myUserDetailsService = myUserDetailsService;

        }

        @Bean
        @Order(1)
        public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
                OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                                .oidc(Customizer.withDefaults())
                                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                                                // Lecture du refresh token dans le cookie HttpOnly
                                                .accessTokenRequestConverter(new OAuth2CookieRefreshTokenConverter())
                                                // Injection du refresh token dans le cookie HttpOnly lors du succès
                                                .accessTokenResponseHandler(new CustomTokenSuccessHandler()));

                http.exceptionHandling(exceptions -> exceptions
                                .defaultAuthenticationEntryPointFor(
                                                new LoginUrlAuthenticationEntryPoint("/login"),
                                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

                http.cors(Customizer.withDefaults());

                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))

                                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)

                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/api/syndics/**").permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(Customizer.withDefaults()))

                                .logout(logout -> logout
                                                .logoutUrl("/api/auth/logout")
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        response.setStatus(HttpServletResponse.SC_OK);
                                                })
                                                .deleteCookies("XSRF-TOKEN")

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
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                // Indispensable pour s'échanger les cookies (JSESSIONID / XSRF-TOKEN)
                configuration.setAllowCredentials(true);
                // Headers autorisés
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "X-XSRF-TOKEN",
                                "Accept",
                                "X-Requested-With"));
                // Headers exposés (pour que React puisse lire certains headers si besoin)
                configuration.setExposedHeaders(Collections.singletonList("X-XSRF-TOKEN"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
