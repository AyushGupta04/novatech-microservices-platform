package com.ecommerce.order.client;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.dto.cart.CartDto;
import com.ecommerce.common.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CartClient {

    private static final Logger log = LoggerFactory.getLogger(CartClient.class);

    private final RestClient restClient;

    public CartClient(
            @Value("${services.cart.url:http://localhost:8084}") String cartUrl,
            RestClient.Builder builder
    ) {
        this.restClient = builder.baseUrl(cartUrl).build();
    }

    public CartDto getCart(Long userId) {
        try {
            ApiResponse<CartDto> response = restClient.get()
                    .uri("/api/v1/cart")
                    .header(SecurityConstants.HEADER_USER_ID, String.valueOf(userId))
                    .header(SecurityConstants.HEADER_USER_ROLE, "ROLE_USER")
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<CartDto>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch cart for user {}: {}", userId, e.getMessage());
        }
        return null;
    }

    public void clearCart(Long userId) {
        try {
            restClient.delete()
                    .uri("/api/v1/cart")
                    .header(SecurityConstants.HEADER_USER_ID, String.valueOf(userId))
                    .header(SecurityConstants.HEADER_USER_ROLE, "ROLE_USER")
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully cleared cart for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to clear cart for user {}: {}", userId, e.getMessage());
        }
    }
}
