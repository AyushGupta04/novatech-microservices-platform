package com.ecommerce.inventory;

import com.ecommerce.common.dto.inventory.StockCheckResponse;
import com.ecommerce.common.dto.inventory.StockReservationRequest;
import com.ecommerce.common.dto.inventory.StockReservationResponse;
import com.ecommerce.common.exception.InsufficientStockException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.inventory.entity.InventoryEntity;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryEntity inventory;

    @BeforeEach
    void setUp() {
        inventory = InventoryEntity.builder()
                .id(1L)
                .sku("LAP-TITAN-16")
                .quantity(20)
                .reservedQuantity(0)
                .build();
    }

    @Test
    @DisplayName("Check stock returns correct availability")
    void testCheckStock() {
        when(inventoryRepository.findBySku("LAP-TITAN-16")).thenReturn(Optional.of(inventory));

        StockCheckResponse response = inventoryService.checkStock("LAP-TITAN-16");

        assertNotNull(response);
        assertEquals(20, response.getAvailableQuantity());
        assertTrue(response.isInStock());
    }

    @Test
    @DisplayName("Check availability returns true when requested <= available")
    void testCheckAvailabilityTrue() {
        when(inventoryRepository.findBySku("LAP-TITAN-16")).thenReturn(Optional.of(inventory));

        StockCheckResponse response = inventoryService.checkAvailability("LAP-TITAN-16", 15);

        assertTrue(response.isInStock());
    }

    @Test
    @DisplayName("Check availability returns false when requested > available")
    void testCheckAvailabilityFalse() {
        when(inventoryRepository.findBySku("LAP-TITAN-16")).thenReturn(Optional.of(inventory));

        StockCheckResponse response = inventoryService.checkAvailability("LAP-TITAN-16", 25);

        assertFalse(response.isInStock());
    }

    @Test
    @DisplayName("Reserve stock successfully updates quantities")
    void testReserveStockSuccess() {
        when(inventoryRepository.findBySkuForUpdate("LAP-TITAN-16")).thenReturn(Optional.of(inventory));

        StockReservationRequest item = StockReservationRequest.builder()
                .sku("LAP-TITAN-16")
                .quantity(5)
                .build();

        StockReservationResponse response = inventoryService.reserveStock(List.of(item));

        assertTrue(response.isSuccessful());
        assertEquals(15, inventory.getQuantity());
        assertEquals(5, inventory.getReservedQuantity());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Reserve stock throws InsufficientStockException when requested > available")
    void testReserveStockInsufficient() {
        when(inventoryRepository.findBySkuForUpdate("LAP-TITAN-16")).thenReturn(Optional.of(inventory));

        StockReservationRequest item = StockReservationRequest.builder()
                .sku("LAP-TITAN-16")
                .quantity(25)
                .build();

        assertThrows(InsufficientStockException.class, () -> inventoryService.reserveStock(List.of(item)));
        assertEquals(20, inventory.getQuantity()); // untouched
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check stock for unknown SKU throws ResourceNotFoundException")
    void testCheckStockNotFound() {
        when(inventoryRepository.findBySku("UNKNOWN-SKU")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.checkStock("UNKNOWN-SKU"));
    }
}
