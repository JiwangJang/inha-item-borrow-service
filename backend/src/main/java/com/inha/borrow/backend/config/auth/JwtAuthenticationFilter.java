package com.inha.borrow.backend.config.auth;

import com.inha.borrow.backend.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException{
        String token = resolveToken(request);
        if(token!=null && jwtTokenService.validateToken(token)){
            String userId = jwtTokenService.getUserId(token);
            List<GrantedAuthority> role = jwtTokenService.getUserAuthorities(token);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId,null,role);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request,response);
    }
    private String resolveToken(HttpServletRequest request){
        String bearer = request.getHeader("Authorization");
        if(bearer!=null&&bearer.startsWith("Bearer")){
            return bearer.substring(7);
        }
        return null;
    }
}
