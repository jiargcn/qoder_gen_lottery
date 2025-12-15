package com.lottery.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和验证 JWT Token
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Value("${lottery.jwt.secret}")
    private String secret;
    
    @Value("${lottery.jwt.expiration}")
    private Long expiration;
    
    /**
     * 生成 JWT Token
     * 
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param username 用户名
     * @param role 角色
     * @return JWT Token
     */
    public String generateToken(String userId, String tenantId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenantId);
        claims.put("username", username);
        claims.put("role", role);
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(key)
                .compact();
    }
    
    /**
     * 从 Token 中解析 Claims
     * 
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析 Token 失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从 Token 中获取用户 ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }
    
    /**
     * 从 Token 中获取租户 ID
     */
    public String getTenantIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("tenantId") : null;
    }
    
    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }
    
    /**
     * 从 Token 中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }
    
    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            
            // 检查是否过期
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            log.error("验证 Token 失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 刷新 Token
     * 
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        
        String userId = claims.getSubject();
        String tenantId = (String) claims.get("tenantId");
        String username = (String) claims.get("username");
        String role = (String) claims.get("role");
        
        return generateToken(userId, tenantId, username, role);
    }
}
