package com.sysmap.hubapi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter por IP para endpoints de autenticação.
 * Limites configuráveis via rate-limit.max-requests e rate-limit.window-ms.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${rate-limit.max-requests:10}")
    private int maxRequests;

    @Value("${rate-limit.window-ms:60000}")
    private long windowMs;

    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler) throws Exception {

        String ip  = resolveClientIp(request);
        long   now = System.currentTimeMillis();

        Deque<Long> timestamps = requestLog.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
                timestamps.pollFirst();
            }
            timestamps.addLast(now);

            if (timestamps.size() > maxRequests) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"error\":\"Muitas tentativas. Tente novamente em 1 minuto.\"}");
                return false;
            }
        }

        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
