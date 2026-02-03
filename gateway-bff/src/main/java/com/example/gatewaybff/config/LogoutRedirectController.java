package com.example.gatewaybff.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
public class LogoutRedirectController {

    @Autowired
    private LogoutProperties appProps;

    @GetMapping("/post-logout")
    public Mono<Void> postLogout(ServerWebExchange exchange) {
        String paramName = appProps.getParam();
        String appTag = exchange.getRequest().getQueryParams().getFirst(paramName);

        String targetUrl = appProps.getRedirect().getApps()
                .getOrDefault(appTag, appProps.getRedirect().getDefault());

        // Redirection HTTP 302
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(targetUrl));
        return exchange.getResponse().setComplete();
    }
}
