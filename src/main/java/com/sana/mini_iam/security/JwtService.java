package com.sana.mini_iam.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
    byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
}

    // Erstellt ein JWT für einen Benutzer
    public String generateToken(String username) {

        Date now = new Date();
        Date expiration = new Date(
            now.getTime() + 1000L * 60 * 60
        );

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact();
    }

    // Liest den Benutzernamen aus einem gültigen JWT
    public String extractUsername(String token) {

        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getSubject();
    }

    public SecretKey getSigningKey() {
    return signingKey;
}
}