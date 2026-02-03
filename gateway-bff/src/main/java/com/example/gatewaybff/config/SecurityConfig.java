package com.example.gatewaybff.config;

import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.server.csrf.CsrfToken;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private LogoutProperties appProps;

    /**
     * Force CSRF token generation/storage for /auth/csrf even if token does not yet
     * exist in session.
     * This guarantees the cookie XSRF-TOKEN is always set after handshake.
     */
    @Bean
    public org.springframework.web.server.WebFilter forceCsrfTokenGeneratingWebFilter() {
        return (exchange, chain) -> {
            if ("/auth/csrf".equals(exchange.getRequest().getPath().value())) {
                org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository repo = org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
                        .withHttpOnlyFalse();
                repo.setCookiePath("/");
                return repo.loadToken(exchange)
                        .switchIfEmpty(
                                repo.generateToken(exchange)
                                        .flatMap(token -> repo.saveToken(exchange, token).then(Mono.just(token))))
                        .then(chain.filter(exchange));
            }
            return chain.filter(exchange);
        };
    }

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public WebFilter subscribeToCsrfTokenWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> token = exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
            return token.doOnSuccess(t -> {
                if (t != null)
                    t.getToken();
            }).then(chain.filter(exchange));
        };
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
            ServerAuthenticationSuccessHandler oauth2SuccessHandler, Environment env) {

        CookieServerCsrfTokenRepository csrfTokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");

        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(exchange -> {
                            String method = exchange.getRequest().getMethod().name();
                            if (Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS").contains(method)) {
                                return ServerWebExchangeMatcher.MatchResult.notMatch();
                            }

                            return new NegatedServerWebExchangeMatcher(
                                    ServerWebExchangeMatchers.pathMatchers(
                                            "/login/**",
                                            "/oauth2/**",
                                            "/auth/csrf",
                                            "/" + env.getProperty("APPA_PATH_PREFIX", "appA") + "/api/auth/register"))
                                    .matches(exchange);
                        }))
                .cors(Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                        .pathMatchers(HttpMethod.GET, "/login").permitAll()
                        .pathMatchers("/oauth2/**").permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/" + env.getProperty("APPA_PATH_PREFIX", "appA") + "/api/syndics/**")
                        .permitAll()
                        .pathMatchers(HttpMethod.POST,
                                "/" + env.getProperty("APPA_PATH_PREFIX", "appA") + "/api/auth/register")
                        .permitAll()
                        .pathMatchers("/auth/user").authenticated()
                        .pathMatchers("/" + env.getProperty("APPA_PATH_PREFIX", "appA") + "/api/**").authenticated()
                        .anyExchange().permitAll())
                .oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(oauth2SuccessHandler))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(oidcLogoutHandler(appProps)))
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

    private String resolveTargetUrl(String app) {
        // 1. On cherche d'abord dans la Map des apps (LogoutProperties)
        String target = appProps.getRedirect().getApps().get(app);

        // 2. Si non trouvé (cas du login ou mapping YAML complexe), on utilise le
        // défaut
        if (target == null || target.isBlank()) {
            target = appProps.getRedirect().getDefault();
        }

        return normalizeBaseUrl(target);
    }

    @Bean
    ServerAuthenticationSuccessHandler oauth2SuccessHandler() {
        return (webFilterExchange, authentication) -> webFilterExchange.getExchange().getSession()
                .flatMap(session -> {
                    String app = (String) session.getAttributes().remove("app");
                    String redirectTarget = resolveTargetUrl(app); // Utilise la méthode harmonisée

                    logger.info("OAuth2 login success. App: {}, Redirecting to: {}", app, redirectTarget);

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
                "X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private ServerLogoutSuccessHandler oidcLogoutHandler(LogoutProperties appProps) {
        return (exchange, authentication) -> exchange.getExchange().getFormData()
                .flatMap(formData -> {
                    // Récupération du tag app
                    String appTag = formData.getFirst(appProps.getParam());
                    if (appTag == null) {
                        appTag = exchange.getExchange().getRequest().getQueryParams().getFirst(appProps.getParam());
                    }

                    String targetUrl = resolveTargetUrl(appTag); // Utilise la même méthode

                    logger.info("Logout triggered. App: {}, Target URL: {}", appTag, targetUrl);

                    OidcClientInitiatedServerLogoutSuccessHandler logoutHandler = new OidcClientInitiatedServerLogoutSuccessHandler(
                            clientRegistrationRepository);

                    // IMPORTANT: On passe l'URL calculée
                    logoutHandler.setPostLogoutRedirectUri(targetUrl);

                    return logoutHandler.onLogoutSuccess(exchange, authentication);
                });
    }
}
