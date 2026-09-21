package com.ecommerce.common;

import com.ecommerce.common.security.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils("EnterpriseSecureSuperSecretKeyForJWTVerificationOnly1234567890!", 3600000L);
    }

    @Test
    @DisplayName("Generate token and validate claims extraction")
    void testTokenGenerationAndParsing() {
        Long userId = 42L;
        String email = "test@enterprise.com";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        String token = jwtUtils.generateToken(userId, email, roles);
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));

        assertEquals(userId, jwtUtils.getUserIdFromToken(token));
        assertEquals(email, jwtUtils.getEmailFromToken(token));

        List<String> parsedRoles = jwtUtils.getRolesFromToken(token);
        assertEquals(2, parsedRoles.size());
        assertTrue(parsedRoles.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Invalid token should fail validation")
    void testInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.token.signature"));
    }
}
