package com.medibook.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    // Generate token
    public String generateToken(Long userId, String email, String role) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder().claims(claims).subject(email).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // extract user
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // extract user id
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    // extract role
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // extract expiration
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // generic claim extractor
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // parse token
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // validate token
    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username)
                && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // signing key
    private SecretKey getSignInKey() {

        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());

        return Keys.hmacShaKeyFor(keyBytes);
    }
}