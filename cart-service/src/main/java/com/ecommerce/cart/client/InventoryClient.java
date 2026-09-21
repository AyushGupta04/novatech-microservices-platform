package com.ecommerce.cart.client;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.dto.inventory.StockCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestClient restClient;

    public InventoryClient(
            @Value("${services.inventory.url:http://localhost:8083}") String inventoryUrl,
            RestClient.Builder builder
    ) {
        this.restClient = builder.baseUrl(inventoryUrl).build();
    }

    public StockCheckResponse checkAvailability(String sku, int quantity) {
        try {
            ApiResponse<StockCheckResponse> response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/inventory/check")
                            .queryParam("sku", sku)
                            .queryParam("quantity", quantity)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<StockCheckResponse>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("Inventory check error for SKU {}: {}", sku, e.getMessage());
        }
        // Fallback: assume available or return false
        return StockCheckResponse.builder().sku(sku).inStock(true).availableQuantity(quantity).build();
    }
}
