package com.example.securitewebback.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FormLoginSuccesHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append(String.format("\"uuid\":\"%s\",", userDetails.getUuid()));
        json.append(String.format("\"role\":\"%s\"", userDetails.getRole()));
        json.append("}");

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        response.getWriter().write(json.toString());
        response.getWriter().flush();
    }
}
