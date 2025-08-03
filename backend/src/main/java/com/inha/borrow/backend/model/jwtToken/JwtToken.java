package com.inha.borrow.backend.model.jwtToken;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtToken {
    private final String JWT_SECRET_KEY = "8B73D9C4956669F1C6EC3FECD6CC18B73D9C4956669F1C6EC3FECD6CC1";
    private final long validityInMilliseconds = 3600000;

    public String createToken(String id) {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("type", "jwt")
                .setSubject(id)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validityInMilliseconds))
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY)
                .compact();
    }

    public String getUserId(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }
}

