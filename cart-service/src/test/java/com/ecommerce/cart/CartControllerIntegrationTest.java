package com.ecommerce.cart;

import com.ecommerce.cart.client.InventoryClient;
import com.ecommerce.cart.client.ProductClient;
import com.ecommerce.common.dto.cart.AddToCartRequest;
import com.ecommerce.common.dto.inventory.StockCheckResponse;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.security.JwtUtils;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerIntegrationTest {

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

    private String userToken;

    @BeforeEach
    void setUp() {
        userToken = "Bearer " + jwtUtils.generateToken(999L, "shopper@ecommerce.com", List.of("ROLE_USER"));

        when(productClient.getProductById(anyLong())).thenReturn(ProductDto.builder()
                .id(1L)
                .name("TitanBook Pro 16")
                .sku("LAP-TITAN-16")
                .price(new BigDecimal("2499.99"))
                .imageUrl("https://example.com/laptop.jpg")
                .build());

        when(inventoryClient.checkAvailability(anyString(), anyInt())).thenReturn(
                StockCheckResponse.builder().sku("LAP-TITAN-16").inStock(true).availableQuantity(50).build()
        );
    }

    @Test
    @DisplayName("GET /api/v1/cart without token returns 401 Unauthorized")
    void testGetCartUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/cart with valid user JWT returns user cart")
    void testGetCartSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(999L));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items adds item and updates totals")
    void testAddToCart() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(1L)
                .sku("LAP-TITAN-16")
                .quantity(1)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].sku").value("LAP-TITAN-16"))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(2499.99));
    }

    @Test
    @DisplayName("DELETE /api/v1/cart clears cart items")
    void testClearCart() throws Exception {
        mockMvc.perform(delete("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
