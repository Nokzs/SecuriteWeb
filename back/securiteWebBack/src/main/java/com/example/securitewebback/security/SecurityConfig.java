package com.example.securitewebback.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;

import com.example.securitewebback.security.SsoConfig.JwtToUserConverter;

import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

                                                                   private final ConcreteUserDetailsService myUserDetailsService;
                                                                   private final JwtToUserConverter jwtToUserConverter;
  
                                                                   public SecurityConfig(ConcreteUserDetailsService myUserDetailsService,
                                                                                                                                                                                                   FormLoginSuccesHandler loginSuccessHandler,
                                                                                                                                                                                                   JwtToUserConverter jwtToUserConverter) {
                                                                                                                                   this.myUserDetailsService = myUserDetailsService;
                                                                                                                                   this.jwtToUserConverter = jwtToUserConverter;


                                                                 }


                                                                @Bean
                                                                @Order(1)
                                                                public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http)
                                                                                                                                                                                                throws Exception {
                                                                                                                                OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

                                                                                                                                OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = http
                                                                                                                                                                                                                                                                .getConfigurer(OAuth2AuthorizationServerConfigurer.class);

                                                                                                                                RequestMatcher authorizationServerEndpointsMatcher = authorizationServerConfigurer
                                                                                                                                                                                                                                                                .getEndpointsMatcher();

                                                                                                                                http.securityMatcher(new OrRequestMatcher(
                                                                                                                                                                                                                                                                authorizationServerEndpointsMatcher,
                                                                                                                                                                                                                                                                request -> "/login".equals(request.getServletPath())));

                                                                                                                                   authorizationServerConfigurer
                                                                                                                                                                                                                                                                   .oidc(Customizer.withDefaults());



                                                                                                                                 http
                                                                                                                                                                                                                                                                 .exceptionHandling(exceptions -> exceptions
                                                                                                                                                                                                                                                                                                                                 .defaultAuthenticationEntryPointFor(
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 new LoginUrlAuthenticationEntryPoint(
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 "/login"),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                                                                                                                                                                                                                                                                 .formLogin(form -> form
                                                                                                                                                                                                                                                                                                                                                                                                 .loginPage("/login")
                                                                                                                                                                                                                                                                                                                                                                                                 .loginProcessingUrl("/login")
                                                                                                                                                                                                                                                                                                                                                                                                 .permitAll());

                                                                                                                                return http.build();
                                                                }

                                                                @Bean
                                                                @Order(2)
                                                                 public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                                                                                                                                 http.securityMatcher("/api/**");

                                                                                                                                  http
                                                                                                                                                                                                                                                                  .csrf(csrf -> csrf.disable())

                                                                                                                                                                                                                                                                  .sessionManagement(session -> session
                                                                                                                                                                                                                                                                                                                                                                                                  .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                                                                                                                                                                                                                                                  .exceptionHandling(ex -> ex
                                                                                                                                                                                                                                                                                                                                                                                                  .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                                                                                                                                                                                                                                                                                                                                                                                                  .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))

                                                                                                                                                                                                                                                                   .authorizeHttpRequests(auth -> auth
                                                                                                                                                                                                                                                                                                                                                                                                   .requestMatchers("/error")
                                                                                                                                                                                                                                                                                                                                                                                                   .permitAll()
                                                                                                                                     .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/syndics/**")
                                                                                                                                     .permitAll()
                                                                                                                                     .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/register")
                                                                                                                                     .permitAll()
                                                                                                                                     .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/owner")
                                                                                                                                     .hasRole("SYNDIC")
                                                                                                                                     .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/change-password")
                                                                                                                                     .hasRole("PROPRIETAIRE")
                                                                                                                                                                                                                                                                                                                                                                                                   .anyRequest()
                                                                                                                                                                                                                                                                                                                                                                                                   .authenticated())

                                                                                                                                    .oauth2ResourceServer(oauth2 -> oauth2
                                                                                                                                                                                                                                                                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtToUserConverter)))


                                                                                                                                                                                                                                                                  .logout(logout -> logout
                                                                                                                                                                                                                                                                                                                                                                                                  .logoutUrl("/api/auth/logout")
                                                                                                                                                                                                                                                                                                                                                                                                  .logoutSuccessHandler((request, response,
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  authentication) -> {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                  response.setStatus(HttpServletResponse.SC_OK);
                                                                                                                                                                                                                                                                                                                                                                                                  }));

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
                                                                                                                                   org.springframework.security.crypto.password.DelegatingPasswordEncoder passwordEncoder = (org.springframework.security.crypto.password.DelegatingPasswordEncoder) org.springframework.security.crypto.factory.PasswordEncoderFactories
                                                                                                                                                                                                                   .createDelegatingPasswordEncoder();
                                                                                                                                   passwordEncoder.setDefaultPasswordEncoderForMatches(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
                                                                                                                                   return passwordEncoder;
                                                                   }



                                                                 @Bean(name = "jwtAuthenticationTokenConverter")
                                                                 public org.springframework.core.convert.converter.Converter<org.springframework.security.oauth2.jwt.Jwt, ? extends org.springframework.security.authentication.AbstractAuthenticationToken> jwtAuthenticationTokenConverter() {
                                                                                                                                 org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter authoritiesConverter = new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter();
                                                                                                                                 authoritiesConverter.setAuthoritiesClaimName("role");
                                                                                                                                 authoritiesConverter.setAuthorityPrefix("ROLE_");

                                                                                                                                 org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter converter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
                                                                                                                                 converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
                                                                                                                                 converter.setPrincipalClaimName("sub");
                                                                                                                                 return converter;
                                                                 }

                                                                 @Bean
                                                                 @org.springframework.context.annotation.Primary
                                                                 public org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter() {
                                                                                                                                 org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter converter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
                                                                                                                                 converter.setJwtGrantedAuthoritiesConverter(jwt -> jwtToUserConverter.convert(jwt).getAuthorities());
                                                                                                                                 converter.setPrincipalClaimName("sub");
                                                                                                                                 return converter;
                                                                 }



}
