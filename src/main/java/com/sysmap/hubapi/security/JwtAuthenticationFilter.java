package com.sysmap.hubapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // TODO: extrair token do header Authorization
        // TODO: validar token e setar autenticação no SecurityContext
        // TODO: verificar se conta está desativada (deleted_at IS NOT NULL) → 403
        filterChain.doFilter(request, response);
    }
}
