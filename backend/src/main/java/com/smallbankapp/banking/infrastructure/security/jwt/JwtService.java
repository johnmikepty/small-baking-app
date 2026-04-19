package com.smallbankapp.banking.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    /**
     * Generates a JWT with userId as subject plus email and accountId claims.
     * The accountId claim allows the Angular frontend to resolve the account
     * without an extra API call on every page load.
     */
    public String generateToken(UUID userId, String email, UUID accountId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("accountId", accountId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + properties.expirationMs()))
                .signWith(signingKey())
                .compact();
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String extractAccountId(String token) {
        return parseClaims(token).get("accountId", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
