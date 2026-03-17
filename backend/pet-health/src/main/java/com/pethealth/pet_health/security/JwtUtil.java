package com.pethealth.pet_health.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(JwtProperties jwtProperties) {
        // 验证密钥长度
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = jwtProperties.getExpiration();
        
        logger.info("JWT initialized with expiration: {} ms", expirationMs);
    }

    /**
     * 生成Token
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            throw new RuntimeException("Token expired", e);
        } catch (SecurityException | MalformedJwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        } catch (JwtException e) {
            logger.warn("JWT error: {}", e.getMessage());
            throw new RuntimeException("Token validation failed", e);
        }
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
        } catch (SecurityException | MalformedJwtException e) {
            logger.warn("Invalid token: {}", e.getMessage());
        } catch (JwtException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取Token过期时间
     */
    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }

    /**
     * 检查Token是否即将过期（比如还剩5分钟）
     */
    public boolean isTokenExpiringSoon(String token) {
        Date expiration = getExpirationDate(token);
        long timeUntilExpiry = expiration.getTime() - System.currentTimeMillis();
        return timeUntilExpiry < 5 * 60 * 1000; // 5分钟
    }
}