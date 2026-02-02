package com.example.gatewaybff.config;

import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Force CSRF token generation/storage for /auth/csrf even if token does not yet exist in session.
     * This guarantees the cookie XSRF-TOKEN is always set after handshake.
     */
    @Bean
    public org.springframework.web.server.WebFilter forceCsrfTokenGeneratingWebFilter() {
        return (exchange, chain) -> {
            if ("/auth/csrf".equals(exchange.getRequest().getPath().value())) {
                org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository repo = org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository.withHttpOnlyFalse();
                repo.setCookiePath("/");
                // Charger ou créer le token, puis poursuivre la chaîne de filtre (ne pas retourner le CsrfToken lui-même)
                return repo.loadToken(exchange)
                .switchIfEmpty(
                    repo.generateToken(exchange)
                        .flatMap(token -> repo.saveToken(exchange, token).then(Mono.just(token)))
                )
                .then(chain.filter(exchange));
            }
            return chain.filter(exchange);
        };
    }


  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
      ServerAuthenticationSuccessHandler oauth2SuccessHandler, Environment env) {
    CookieServerCsrfTokenRepository csrfTokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookiePath("/");

    return http
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository))
        .cors(Customizer.withDefaults())
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeExchange(ex -> ex
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
            .pathMatchers(HttpMethod.GET, "/" + env.getProperty("APPA_PATH_PREFIX", "appA") + "/api/syndics/**")
            .permitAll()
            .pathMatchers(HttpMethod.POST, "/" + env.getProperty("APPA_PATH_PREFIX", "appA") + "/api/auth/register")
            .permitAll()
             .pathMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
             .pathMatchers(HttpMethod.GET, "/login").permitAll()
             .pathMatchers("/auth/user").authenticated()
             .pathMatchers("/" + env.getProperty("APPA_PATH_PREFIX", "appA") + "/api/**").authenticated()
            .anyExchange().permitAll())
        .oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(oauth2SuccessHandler))
        .logout(logout -> logout.logoutUrl("/auth/logout"))
        .build();
  }

  @Bean
  WebFilter oauth2AppHintFilter(org.springframework.core.env.Environment env) {
    return (exchange, chain) -> {
      String path = exchange.getRequest().getPath().value();
      if (!path.startsWith("/oauth2/authorization/")) {
        return chain.filter(exchange);
      }

       String appParamName = env.getProperty("app.param", "app");
       String app = exchange.getRequest().getQueryParams().getFirst(appParamName);

       logger.info("OAuth2 authorization entry: path={}, {}={}", path, appParamName, app);

       if (app == null || app.isBlank()) {
         return chain.filter(exchange);
       }

       return exchange.getSession()
           .doOnNext(session -> {
             session.getAttributes().put("app", app);
             logger.info("Stored app in session: id={}, app={}", session.getId(), app);
           })
           .then(chain.filter(exchange));
    };
  }

  @Bean
   ServerAuthenticationSuccessHandler oauth2SuccessHandler(org.springframework.core.env.Environment env) {
     return (webFilterExchange, authentication) -> webFilterExchange.getExchange().getSession()
         .flatMap(session -> {
           String app = (String) session.getAttributes().remove("app");

           logger.info("OAuth2 success: session id={}, app={}", session.getId(), app);

           String redirectTarget = resolveRedirectTarget(env, app);

           logger.info("OAuth2 redirecting to: {}", redirectTarget);

           RedirectServerAuthenticationSuccessHandler delegate = new RedirectServerAuthenticationSuccessHandler(
               redirectTarget);
           return delegate.onAuthenticationSuccess(webFilterExchange, authentication);
         });
   }

   private static String resolveRedirectTarget(Environment env, String app) {
     String defaultTarget = env.getProperty("app.redirect.default", "http://localhost:3000/");

     if (app == null || app.isBlank()) {
       return normalizeBaseUrl(defaultTarget);
     }

     String override = env.getProperty("app.redirect." + app);
     if (override == null || override.isBlank()) {
       return normalizeBaseUrl(defaultTarget);
     }

     return normalizeBaseUrl(override);
   }

   private static String normalizeBaseUrl(String url) {
     if (url == null || url.isBlank()) {
       return "/";
     }
     if (url.endsWith("/")) {
       return url;
     }
     return url + "/";
   }

  @Bean
  CorsConfigurationSource corsConfigurationSource(org.springframework.core.env.Environment env) {
    CorsConfiguration config = new CorsConfiguration();

    String rawAllowedOrigins = env.getProperty("app.cors.allowed-origins",
        "http://localhost:3000,http://localhost:3001");
    List<String> allowedOrigins = Arrays.stream(rawAllowedOrigins.split(","))
        .map(String::trim)
        .filter(origin -> !origin.isBlank())
        .collect(Collectors.toList());

    config.setAllowedOrigins(allowedOrigins);
    config.setAllowCredentials(true);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of(
        "Authorization",
        "Content-Type",
        "X-Requested-With",
        "Accept",
        "X-XSRF-TOKEN"
    ));
    config.setExposedHeaders(List.of("Set-Cookie"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
