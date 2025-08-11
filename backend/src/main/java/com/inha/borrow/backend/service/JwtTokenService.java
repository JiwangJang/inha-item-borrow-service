package com.inha.borrow.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

import javax.crypto.SecretKey;

@Service
public class JwtTokenService {
    @Value("${app.jwt.secret-key")
    private String JWT_SECRET_KEY;
    // 리프레시 토큰의 만료일은 10일로 준다, 리프레시 토큰이 만로됐을경우 기기에 저장된 아이디 비번 활용해서 재로그인후 다시 발급
    private final long EXPIRE_TIME = 864_000_000;

    private SecretKey getSignKey() {
        return Jwts.SIG.HS256.key().build();
    }

    public String createToken(String id) {
        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .subject(id)
                .signWith(getSignKey())
                .compact();
    }

    public String getUserId(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token);
        return jws.getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parser().verifyWith(getSignKey()).build();
            parser.parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
