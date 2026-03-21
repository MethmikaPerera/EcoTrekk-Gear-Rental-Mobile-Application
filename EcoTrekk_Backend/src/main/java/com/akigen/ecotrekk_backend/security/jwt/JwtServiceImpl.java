package com.akigen.ecotrekk_backend.security.jwt;

import com.akigen.ecotrekk_backend.config.JwtProperties;
import com.akigen.ecotrekk_backend.entity.AdminUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAdminAccessToken(AdminUser adminUser) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.getExpirationInSeconds());

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(adminUser.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("type", "ADMIN")
            .claim("name", adminUser.getFullName())
            .claim("fullName", adminUser.getFullName())
                .claim("username", adminUser.getUsername())
                .claim("role", adminUser.getRole().name())
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public long getAccessTokenExpiresInSeconds() {
        return jwtProperties.getExpirationInSeconds();
    }

    @Override
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
