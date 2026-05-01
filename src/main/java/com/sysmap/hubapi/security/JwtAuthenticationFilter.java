package com.sysmap.hubapi.security;

import com.sysmap.hubapi.entity.User;
import com.sysmap.hubapi.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);

            userRepository.findById(userId).ifPresent(user -> {
                if (user.getDeletedAt() != null) {
                    try {
                        writeForbidden(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                setAuthentication(request, user);
            });

            if (response.isCommitted()) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void setAuthentication(HttpServletRequest request, User user) {
        var auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        String body = "{\"error\":\"Esta conta foi desativada e não pode ser utilizada.\"}";
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.setContentLength(body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        response.getWriter().write(body);
        response.getWriter().flush();
    }
}
