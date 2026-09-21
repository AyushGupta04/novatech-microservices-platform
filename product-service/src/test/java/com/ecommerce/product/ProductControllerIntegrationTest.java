package com.ecommerce.product;

import com.ecommerce.common.dto.product.ProductCreateRequest;
import com.ecommerce.common.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminToken = "Bearer " + jwtUtils.generateToken(1L, "admin@ecommerce.com", List.of("ROLE_ADMIN", "ROLE_USER"));
        userToken = "Bearer " + jwtUtils.generateToken(2L, "user@ecommerce.com", List.of("ROLE_USER"));
    }

    @Test
    @DisplayName("GET /api/v1/products returns seeded products with 200 OK")
    void testGetProductsList() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/products with search query filters correctly")
    void testSearchProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("search", "TitanBook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name", containsString("TitanBook")));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} returns product details")
    void testGetProductById() throws Exception {
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sku").value("LAP-TITAN-16"));
    }

    @Test
    @DisplayName("POST /api/v1/products without token returns 401 Unauthorized")
    void testCreateProductUnauthenticated() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Unauthorized Product")
                .price(new BigDecimal("99.99"))
                .sku("UNAUTH-SKU-1")
                .categoryId(1L)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/products with USER role token returns 403 Forbidden")
    void testCreateProductForbiddenForUser() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("User Created Product")
                .price(new BigDecimal("99.99"))
                .sku("USER-SKU-1")
                .categoryId(1L)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/products with ADMIN role token creates product and returns 201")
    void testCreateProductAdminSuccess() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Pro Wireless Mouse")
                .description("Ergonomic high precision optical mouse")
                .price(new BigDecimal("79.99"))
                .sku("ACC-MOUSE-PRO")
                .categoryId(1L)
                .initialStock(100)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Pro Wireless Mouse"))
                .andExpect(jsonPath("$.data.sku").value("ACC-MOUSE-PRO"))
                .andExpect(jsonPath("$.data.stockQuantity").value(100));
    }
}
