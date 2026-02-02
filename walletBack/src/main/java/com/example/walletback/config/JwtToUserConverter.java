package com.example.walletback.config;

import com.example.walletback.entities.User;
import com.example.walletback.repository.UserRepository;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class JwtToUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;
    private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public JwtToUserConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String subject = jwt.getSubject();

        UUID ssoID = UUID.fromString(subject);

        String email = jwt.getClaimAsString("email");
        String role = jwt.getClaimAsString("role");

        userRepository.findBySsoId(ssoID).orElseGet(() -> {
            User newUser = User.builder()
                    .ssoId(ssoID)
                    .email(email)
                    .role(role)
                    .balance(BigDecimal.ZERO)
                    .build();
            return userRepository.save(newUser);
        });

        return new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt), subject);
    }
}
