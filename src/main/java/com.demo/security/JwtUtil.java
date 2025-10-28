package com.demo.security;

import com.demo.model.AdminUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Utility Class
 * Handles JWT token generation, validation, and claims extraction
 */
public class JwtUtil {
    private String secret;
    private Long expiration;
    private SecretKey secretKey;

    public void setSecret(String secret) {
        this.secret = secret;
        // Generate SecretKey from secret string
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    /**
     * Generate JWT token for admin user
     */
    public String generateToken(AdminUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        claims.put("email", user.getEmail());
        
        return createToken(claims, user.getUsername());
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Invalid JWT token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract user ID from token
     */
    public Integer extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("userId", Integer.class);
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extract all claims from token
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get remaining time before token expires (in milliseconds)
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - new Date().getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Check if token needs refresh (less than 1 hour remaining)
     */
    public boolean needsRefresh(String token) {
        long remainingTime = getRemainingTime(token);
        long oneHour = 60 * 60 * 1000; // 1 hour in milliseconds
        return remainingTime > 0 && remainingTime < oneHour;
    }
}
