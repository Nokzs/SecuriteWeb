package com.example.securitewebback.security.SsoConfig;

import java.security.KeyPairGenerator;

import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.security.CustomUserDetails;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.oauth2.core.OAuth2Token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .accessTokenTimeToLive(Duration.of(15, ChronoUnit.MINUTES))
                .refreshTokenTimeToLive(Duration.of(30, ChronoUnit.DAYS))
                .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                .reuseRefreshTokens(false)
                .build();

        String clientId = getEnvOrDefault("OAUTH2_GATEWAY_CLIENT_ID", "gateway-client");
        String clientSecret = getEnvOrDefault("OAUTH2_GATEWAY_CLIENT_SECRET", "secret-secret");
        String redirectUrisCsv = getEnvOrDefault(
                "OAUTH2_GATEWAY_REDIRECT_URIS",
                "http://localhost:8082/login/oauth2/code/gateway-client");
        String postLogoutRedirectUrisCsv = getEnvOrDefault(
                "OAUTH2_POST_LOGOUT_REDIRECT_URIS",
                "http://localhost:3000/");

        RegisteredClient.Builder gatewayClientBuilder = RegisteredClient.withId(clientId)
                .clientId(clientId)
                .clientSecret("{noop}" + clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("email")
                .scope("offline_access")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(false)
                        .build())
                .tokenSettings(tokenSettings);

        for (String uri : splitCsv(redirectUrisCsv)) {
            gatewayClientBuilder.redirectUri(uri);
        }
        for (String uri : splitCsv(postLogoutRedirectUrisCsv)) {
            gatewayClientBuilder.postLogoutRedirectUri(uri);
        }

        RegisteredClient gatewayClient = gatewayClientBuilder.build();

        return new InMemoryRegisteredClientRepository(gatewayClient);
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static String[] splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return new String[0];
        }
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator(JWKSource<SecurityContext> jwkSource,
            OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer) {
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(tokenCustomizer);

        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();

        org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator refreshTokenGenerator = new org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            Authentication principal = context.getPrincipal();
            CustomUserDetails user = (CustomUserDetails) principal.getPrincipal();
            User realUser = user.getUser();

            context.getClaims().claims(claims -> {
                // Ces claims iront dans l'Access Token ET l'ID Token
                claims.put("role", realUser.getRole());
                claims.put("email", realUser.getEmail()); // Ton champ mail

                // "sub" doit 5tre un identifiant stable (UUID) pour le Resource Server
                claims.put("sub", realUser.getId().toString());
            });

            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claim("isFirstLogin", realUser.getIsFirstLogin());
            }
        };
    }

}
