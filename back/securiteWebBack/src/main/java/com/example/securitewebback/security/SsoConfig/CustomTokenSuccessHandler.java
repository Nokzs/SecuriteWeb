package com.example.securitewebback.security.SsoConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CustomTokenSuccessHandler implements AuthenticationSuccessHandler {

        private final OAuth2AccessTokenResponseHttpMessageConverter tokenHttpResponseConverter = new OAuth2AccessTokenResponseHttpMessageConverter();

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException {
                OAuth2AccessTokenAuthenticationToken accessTokenAuthentication = (OAuth2AccessTokenAuthenticationToken) authentication;

                OAuth2AccessToken accessToken = accessTokenAuthentication.getAccessToken();
                OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse
                                .withToken(accessToken.getTokenValue())
                                .tokenType(accessToken.getTokenType())
                                .scopes(accessToken.getScopes());

                if (accessTokenAuthentication.getRefreshToken() != null) {
                        String refreshToken = accessTokenAuthentication.getRefreshToken().getTokenValue();
                        builder.refreshToken(refreshToken);

                        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                                        .httpOnly(true)
                                        .secure(false)
                                        .path("/")
                                        .maxAge(Duration.ofDays(30))
                                        .sameSite("Lax")
                                        .build();

                        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                }

                Object idToken = accessTokenAuthentication.getAdditionalParameters().get("id_token");
                if (idToken != null) {
                        builder.additionalParameters(Map.of("id_token", idToken));
                }

                OAuth2AccessTokenResponse accessTokenResponse = builder.build();
                ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);

                this.tokenHttpResponseConverter.write(accessTokenResponse, null, httpResponse);
        }
}
