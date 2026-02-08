package com.example.gatewaybff.web;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
public class UserController {

  @GetMapping("/auth/csrf")
  public Mono<Map<String, Object>> csrf(ServerWebExchange exchange) {
    Mono<CsrfToken> csrfTokenMono = exchange.getAttribute(CsrfToken.class.getName());

    if (csrfTokenMono == null) {
      return Mono.just(Map.of("token", "null"));
    }

    return csrfTokenMono
        .map(csrfToken -> Map.<String, Object>of(
            "headerName", csrfToken.getHeaderName(),
            "parameterName", csrfToken.getParameterName(),
            "token", csrfToken.getToken()))
        .defaultIfEmpty(Map.of("token", "null"));
  }

  @GetMapping("/login")
  public Mono<Void> login(org.springframework.web.server.ServerWebExchange exchange,
      @org.springframework.web.bind.annotation.RequestParam(name = "app", required = false) String app) {
    return exchange.getSession()
        .doOnNext(session -> {
          if (app != null && !app.isBlank()) {
            session.getAttributes().put("app", app);
          }
        })
        .then(Mono.defer(() -> {
          org.springframework.http.HttpHeaders headers = exchange.getResponse().getHeaders();
          headers.setLocation(java.net.URI.create("/oauth2/authorization/gateway-client"));
          exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
          return exchange.getResponse().setComplete();
        }));
  }

  @GetMapping("/auth/user")
  public Mono<Map<String, Object>> me(@AuthenticationPrincipal OidcUser user) {
    if (user == null) {
      return Mono.just(Map.of("authenticated", false));
    }

    String email = user.getEmail();
    String name = user.getFullName() != null ? user.getFullName() : user.getName();

    String sub = user.getSubject();
    String role = user.getClaimAsString("role");
    Boolean isFirstLogin = user.getClaimAsBoolean("isFirstLogin");
    List<String> roles = user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    if (role != null && !role.isBlank()) {
      roles = java.util.stream.Stream.concat(
          roles.stream(),
          java.util.stream.Stream.of("ROLE_" + role))
          .distinct()
          .toList();
    }

    return Mono.just(Map.of(
        "authenticated", true,
        "sub", sub,
        "name", name,
        "email", email,
        "role", role,
        "roles", roles,
        "isFirstLogin", isFirstLogin));
  }
}
