package com.fn_tracking.fitnessapi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    // Thay đổi kiểu trả về thành SecretKey cho bản JJWT 0.12.x
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // 1. Tạo JWT Token từ Username (Cú pháp mới)
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(username)       // Bỏ chữ "set"
                .issuedAt(new Date())    // Bỏ chữ "set"
                .expiration(expiryDate)  // Bỏ chữ "set"
                .signWith(getSigningKey()) // Không cần truyền cứng SignatureAlgorithm nữa
                .compact();
    }

    // 2. Trích xuất Username từ Token (Cú pháp mới)
    public String getUsernameFromToken(String token) {
        return Jwts.parser()                      // Dùng parser() thay vì parserBuilder()
                .verifyWith(getSigningKey())      // Dùng verifyWith() thay vì setSigningKey()
                .build()
                .parseSignedClaims(token)         // Dùng parseSignedClaims()
                .getPayload()                     // Dùng getPayload() thay vì getBody()
                .getSubject();
    }

    // 3. Kiểm tra tính hợp lệ của Token
    public boolean validateToken(String token) {
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
}