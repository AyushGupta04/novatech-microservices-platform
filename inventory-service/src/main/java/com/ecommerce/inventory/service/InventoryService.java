package com.ecommerce.inventory.service;

import com.ecommerce.common.dto.inventory.StockCheckResponse;
import com.ecommerce.common.dto.inventory.StockReservationRequest;
import com.ecommerce.common.dto.inventory.StockReservationResponse;
import com.ecommerce.common.dto.inventory.StockUpdateRequest;
import com.ecommerce.common.exception.DuplicateResourceException;
import com.ecommerce.common.exception.InsufficientStockException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.inventory.entity.InventoryEntity;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(String sku) {
        String normalizedSku = sku.trim().toUpperCase();
        InventoryEntity inventory = inventoryRepository.findBySku(normalizedSku)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "sku", normalizedSku));

        return StockCheckResponse.builder()
                .sku(inventory.getSku())
                .availableQuantity(inventory.getQuantity())
                .inStock(inventory.getQuantity() > 0)
                .build();
    }

    @Transactional(readOnly = true)
    public StockCheckResponse checkAvailability(String sku, int requestedQuantity) {
        String normalizedSku = sku.trim().toUpperCase();
        InventoryEntity inventory = inventoryRepository.findBySku(normalizedSku)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "sku", normalizedSku));

        boolean available = inventory.getQuantity() >= requestedQuantity;
        return StockCheckResponse.builder()
                .sku(inventory.getSku())
                .availableQuantity(inventory.getQuantity())
                .inStock(available)
                .build();
    }

    /**
     * Atomically reserves stock for a list of items using pessimistic locking.
     * Requests are sorted by SKU to guarantee consistent lock acquisition ordering and avoid deadlocks.
     */
    @Transactional
    public StockReservationResponse reserveStock(List<StockReservationRequest> items) {
        if (items == null || items.isEmpty()) {
            return StockReservationResponse.builder()
                    .successful(true)
                    .message("No items to reserve")
                    .items(List.of())
                    .build();
        }

        // Sort items by SKU alphabetically to prevent database deadlocks across concurrent requests
        List<StockReservationRequest> sortedItems = items.stream()
                .sorted(Comparator.comparing(StockReservationRequest::getSku))
                .toList();

        for (StockReservationRequest item : sortedItems) {
            String sku = item.getSku().trim().toUpperCase();
            int qty = item.getQuantity();

            InventoryEntity inventory = inventoryRepository.findBySkuForUpdate(sku)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory", "sku", sku));

            if (inventory.getQuantity() < qty) {
                log.warn("Stock reservation failed for SKU {}: requested {}, available {}", sku, qty, inventory.getQuantity());
                throw new InsufficientStockException(sku, qty, inventory.getQuantity());
            }

            inventory.setQuantity(inventory.getQuantity() - qty);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + qty);
            inventoryRepository.save(inventory);

            log.info("Reserved {} units of SKU {}. Remaining available: {}", qty, sku, inventory.getQuantity());
        }

        return StockReservationResponse.builder()
                .successful(true)
                .message("Stock reserved successfully")
                .items(sortedItems)
                .build();
    }

    /**
     * Releases reserved stock back to available inventory on order cancellation or failure.
     */
    @Transactional
    public StockReservationResponse releaseStock(List<StockReservationRequest> items) {
        if (items == null || items.isEmpty()) {
            return StockReservationResponse.builder()
                    .successful(true)
                    .message("No items to release")
                    .items(List.of())
                    .build();
        }

        for (StockReservationRequest item : items) {
            String sku = item.getSku().trim().toUpperCase();
            int qty = item.getQuantity();

            inventoryRepository.releaseStock(sku, qty);
            log.info("Released {} units of SKU {} back to inventory", qty, sku);
        }

        return StockReservationResponse.builder()
                .successful(true)
                .message("Stock released successfully")
                .items(items)
                .build();
    }

    @Transactional
    public StockCheckResponse updateStock(StockUpdateRequest request) {
        String sku = request.getSku().trim().toUpperCase();
        InventoryEntity inventory = inventoryRepository.findBySku(sku)
                .orElseGet(() -> InventoryEntity.builder()
                        .sku(sku)
                        .quantity(0)
                        .reservedQuantity(0)
                        .build());

        inventory.setQuantity(request.getQuantity());
        InventoryEntity saved = inventoryRepository.save(inventory);

        return StockCheckResponse.builder()
                .sku(saved.getSku())
                .availableQuantity(saved.getQuantity())
                .inStock(saved.getQuantity() > 0)
                .build();
    }

    @Transactional
    public StockCheckResponse createInventory(String sku, int initialStock) {
        String normalizedSku = sku.trim().toUpperCase();
        if (inventoryRepository.existsBySku(normalizedSku)) {
            throw new DuplicateResourceException("Inventory for SKU '" + normalizedSku + "' already exists");
        }

        InventoryEntity inventory = InventoryEntity.builder()
                .sku(normalizedSku)
                .quantity(Math.max(0, initialStock))
                .reservedQuantity(0)
                .build();

        InventoryEntity saved = inventoryRepository.save(inventory);
        return StockCheckResponse.builder()
                .sku(saved.getSku())
                .availableQuantity(saved.getQuantity())
                .inStock(saved.getQuantity() > 0)
                .build();
    }
}
