package com.calendar.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;             // 🔥 新增這行
import org.slf4j.LoggerFactory;      // 🔥 新增這行
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    
	// 🔥 新增這行：手動宣告 Logger
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
	
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    private SecretKey key;

    // 🔥 優化：初始化時就生成好 Key，不用每次請求都重新做
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("無效的 JWT 簽名: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已過期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支援的 JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Claims 字串為空: {}", e.getMessage());
        }
        return false;
    }
}