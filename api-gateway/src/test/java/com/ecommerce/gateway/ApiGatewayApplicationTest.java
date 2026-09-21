package com.ecommerce.gateway;

import com.ecommerce.common.security.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ApiGatewayApplicationTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("Verify API Gateway Spring Context loads successfully")
    void contextLoads() {
        assertNotNull(jwtUtils);
    }
}
