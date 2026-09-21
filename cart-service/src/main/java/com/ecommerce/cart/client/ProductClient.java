package com.ecommerce.cart.client;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    private final RestClient restClient;

    public ProductClient(
            @Value("${services.product.url:http://localhost:8082}") String productUrl,
            RestClient.Builder builder
    ) {
        this.restClient = builder.baseUrl(productUrl).build();
    }

    public ProductDto getProductById(Long productId) {
        try {
            ApiResponse<ProductDto> response = restClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<ProductDto>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to fetch product with id {}: {}", productId, e.getMessage());
        }
        throw new ResourceNotFoundException("Product", "id", productId);
    }
}
