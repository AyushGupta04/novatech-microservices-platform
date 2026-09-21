package com.ecommerce.common.security;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String DEFAULT_JWT_SECRET = "EnterpriseEcommerceSecretKeyMustBeAtLeast256BitsLongForHMACSHA256!";
    public static final long ACCESS_TOKEN_EXPIRATION_MS = 1000L * 60 * 60; // 1 hour
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 1000L * 60 * 60 * 24 * 7; // 7 days
}
