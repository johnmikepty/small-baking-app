package com.smallbankapp.banking.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP-based rate limiting filter using Bucket4j token bucket algorithm.
 *
 * Limits:
 * - Auth endpoints (/api/auth/**): 10 requests per minute (brute-force protection)
 * - All other endpoints:          60 requests per minute
 *
 * Returns HTTP 429 Too Many Requests when limit is exceeded.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int AUTH_CAPACITY      = 10;
    private static final int AUTH_REFILL_MINS   = 1;
    private static final int API_CAPACITY       = 60;
    private static final int API_REFILL_MINS    = 1;

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets  = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ip  = resolveClientIp(request);
        String uri = request.getRequestURI();

        Bucket bucket = uri.startsWith("/api/auth")
                ? authBuckets.computeIfAbsent(ip, k -> newBucket(AUTH_CAPACITY, AUTH_REFILL_MINS))
                : apiBuckets.computeIfAbsent(ip, k -> newBucket(API_CAPACITY, API_REFILL_MINS));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP={} URI={}", ip, uri);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"type":"about:blank","title":"Too Many Requests",
                     "status":429,"detail":"Rate limit exceeded. Please try again later."}
                    """);
        }
    }

    private Bucket newBucket(int capacity, int refillMinutes) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, Duration.ofMinutes(refillMinutes))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
