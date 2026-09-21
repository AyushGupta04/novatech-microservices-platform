package com.ecommerce.gateway.filter;

import com.ecommerce.common.security.JwtUtils;
import com.ecommerce.common.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;

    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator"
    );

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // Check if request is to an open endpoint
        boolean isPublicPath = OPEN_API_ENDPOINTS.stream().anyMatch(path::startsWith);
        boolean isPublicGet = (method == HttpMethod.GET) &&
                (path.startsWith("/api/v1/products") || path.startsWith("/api/v1/categories") || path.startsWith("/api/v1/inventory"));

        String token = extractToken(request);

        if (isPublicPath || isPublicGet) {
            // Optional token propagation if present
            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
                ServerHttpRequest mutatedRequest = injectUserHeaders(request, token);
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }
            return chain.filter(exchange);
        }

        // Protected Endpoint: Token is required
        if (!StringUtils.hasText(token) || !jwtUtils.validateToken(token)) {
            log.warn("Unauthorized access attempt to secured path: {}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        ServerHttpRequest mutatedRequest = injectUserHeaders(request, token);
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private ServerHttpRequest injectUserHeaders(ServerHttpRequest request, String token) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        String email = jwtUtils.getEmailFromToken(token);
        List<String> roles = jwtUtils.getRolesFromToken(token);

        return request.mutate()
                .header(SecurityConstants.HEADER_USER_ID, String.valueOf(userId))
                .header(SecurityConstants.HEADER_USER_EMAIL, email)
                .header(SecurityConstants.HEADER_USER_ROLE, roles != null ? String.join(",", roles) : "")
                .build();
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // High precedence in filter chain
    }
}
