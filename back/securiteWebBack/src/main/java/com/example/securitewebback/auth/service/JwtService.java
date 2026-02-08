package com.example.securitewebback.auth.service;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.security.CustomUserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(CustomUserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        User user = userDetails.getUser();
        // Toutes les infos sont packagées ici (incognito dans le payload)
        extraClaims.put("uuid", user.getId());
        extraClaims.put("role", user.getRole());
        extraClaims.put("isFirstLogin", user.getIsFirstLogin());
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUuid().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
