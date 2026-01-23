import java.util.Arrays;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
                private final ConcreteUserDetailsService userDetailsService;

                private final CsrfTokenRequestHandler requestHandler = new XorCsrfTokenRequestAttributeHandler();

                public SecurityConfig(ConcreteUserDetailsService userDetailsService) {
                                this.userDetailsService = userDetailsService;
                }

                @Bean
                public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                                http
                                                                .csrf(csrf -> csrf
                                                                                                .csrfTokenRepository(CookieCsrfTokenRepository
                                                                                                                                .withHttpOnlyFalse())
                                                                                                .csrfTokenRequestHandler(requestHandler))
                                                                .addFilterAfter(new CsrfCookieFilter(),
                                                                                                BasicAuthenticationFilter.class)
                                                                .cors(cors -> cors.configurationSource(
                                                                                                corsConfigurationSource()))
                                                                .authorizeHttpRequests(auth -> auth
                                                                                                .requestMatchers("/api/auth/register/**")
                                                                                                .permitAll()
                                                                                                .anyRequest()
                                                                                                .authenticated())
                                                                .sessionManagement(session -> session
                                                                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Créé
                                                                                                                                                          // seulement
                                                                                                                                                          // si
                                                                                                                                                          // nécessaire
                                                                                                .maximumSessions(1))
                                                                .formLogin(form -> form.usernameParameter("email")
                                                                                                .loginProcessingUrl("/api/auth/login")
                                                                                                .successHandler((request, response,
                                                                                                                                authentication) -> {
                                                                                                                response.setStatus(200);
                                                                                                                response.setContentType("application/json;charset=UTF-8");
                                                                                                                response.getWriter().write(
                                                                                                                                                "{\"status\": \"SUCCESS\", \"message\": \"Connecté\"}");
                                                                                                })
                                                                                                .failureHandler((request, response,
                                                                                                                                exception) -> {
                                                                                                                response.setStatus(401);
                                                                                                                response.setContentType("application/json;charset=UTF-8");
                                                                                                                response.getWriter().write(
                                                                                                                                                "{\"status\": \"ERROR\", \"message\": \"Identifiants incorrects\"}");
                                                                                                }))
                                                                .logout(logout -> logout
                                                                                                .logoutUrl("/api/auth/logout")
                                                                                                .logoutSuccessHandler((request, response,
                                                                                                                                authentication) -> response
                                                                                                                                                                .setStatus(200)));

                                return http.build();
                }

                @Bean
                public DaoAuthenticationProvider authenticationProvider() {
                                DaoAuthenticationProvider authProvider;
                                authProvider = new DaoAuthenticationProvider(this.userDetailsService);
                                authProvider.setPasswordEncoder(passwordEncoder()); // On lie ton encodeur (BCrypt)

                                return authProvider;
                }

                @Bean
                public PasswordEncoder passwordEncoder() {
                                return new BCryptPasswordEncoder();
                }

                @Bean
                public CorsConfigurationSource corsConfigurationSource() {
                                CorsConfiguration configuration = new CorsConfiguration();
                                configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
                                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                                configuration.setAllowCredentials(true);
                                configuration.setAllowedHeaders(
                                                                Arrays.asList("Authorization", "Content-Type",
                                                                                                "X-XSRF-TOKEN"));

                                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                                source.registerCorsConfiguration("/**", configuration);
                                return source;
                }
}
