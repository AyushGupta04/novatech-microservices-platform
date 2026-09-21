package com.ecommerce.inventory;

import com.ecommerce.common.dto.inventory.StockReservationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/inventory/{sku} returns seeded inventory")
    void testGetStock() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/LAP-TITAN-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("LAP-TITAN-16"))
                .andExpect(jsonPath("$.data.availableQuantity").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.inStock").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/reserve successfully reserves stock")
    void testReserveStock() throws Exception {
        List<StockReservationRequest> items = List.of(
                StockReservationRequest.builder().sku("AUD-SONIC-NC").quantity(2).build()
        );

        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.successful").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/reserve with excessive quantity returns 400 Insufficient Stock")
    void testReserveStockExcessive() throws Exception {
        List<StockReservationRequest> items = List.of(
                StockReservationRequest.builder().sku("MON-CURVED-34").quantity(99999).build()
        );

        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient Stock"))
                .andExpect(jsonPath("$.message", containsString("Insufficient stock for SKU")));
    }
}
