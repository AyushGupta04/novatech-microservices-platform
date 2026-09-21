package com.ecommerce.order.client;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.dto.inventory.StockReservationRequest;
import com.ecommerce.common.dto.inventory.StockReservationResponse;
import com.ecommerce.common.exception.InsufficientStockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

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

    public void reserveStock(List<StockReservationRequest> items) {
        try {
            ApiResponse<StockReservationResponse> response = restClient.post()
                    .uri("/api/v1/inventory/reserve")
                    .body(items)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        throw new InsufficientStockException("One or more items in the order are out of stock");
                    })
                    .body(new ParameterizedTypeReference<ApiResponse<StockReservationResponse>>() {});

            if (response == null || !response.isSuccess()) {
                throw new InsufficientStockException("Stock reservation failed for requested items");
            }
        } catch (InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            log.error("Inventory reservation error: {}", e.getMessage());
            throw new InsufficientStockException("Unable to reserve inventory for order: " + e.getMessage());
        }
    }

    public void releaseStock(List<StockReservationRequest> items) {
        try {
            restClient.post()
                    .uri("/api/v1/inventory/release")
                    .body(items)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully requested stock release for {} items", items.size());
        } catch (Exception e) {
            log.error("Failed to release stock: {}", e.getMessage());
        }
    }
}
