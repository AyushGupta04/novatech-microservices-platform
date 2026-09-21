package com.ecommerce.order;

import com.ecommerce.common.dto.order.CreateOrderRequest;
import com.ecommerce.common.dto.order.OrderItemRequest;
import com.ecommerce.common.dto.order.UpdateOrderStatusRequest;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.security.JwtUtils;
import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.ProductClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private ProductClient productClient;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private CartClient cartClient;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userToken = "Bearer " + jwtUtils.generateToken(2L, "user@ecommerce.com", List.of("ROLE_USER"));
        adminToken = "Bearer " + jwtUtils.generateToken(1L, "admin@ecommerce.com", List.of("ROLE_ADMIN", "ROLE_USER"));

        when(productClient.getProductById(anyLong())).thenReturn(ProductDto.builder()
                .id(3L)
                .sku("AUD-SONIC-NC")
                .name("SonicWave Noise-Cancelling Headphones")
                .price(new BigDecimal("349.50"))
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/orders returns seeded orders for user")
    void testGetUserOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} returns order details")
    void testGetOrderDetails() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-20260901-0001"))
                .andExpect(jsonPath("$.data.totalAmount").value(349.50));
    }

    @Test
    @DisplayName("POST /api/v1/orders places order and snapshots price")
    void testCreateOrder() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("789 Pine Ave, Seattle, WA")
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId(3L)
                                .sku("AUD-SONIC-NC")
                                .quantity(1)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.items[0].productName").value("SonicWave Noise-Cancelling Headphones"))
                .andExpect(jsonPath("$.data.items[0].unitPrice").value(349.50));
    }

    @Test
    @DisplayName("GET /api/v1/orders/admin forbidden for non-admin user")
    void testAdminOrdersForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/orders/admin")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/orders/admin accessible for admin")
    void testAdminOrdersAllowedForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/orders/admin")
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
