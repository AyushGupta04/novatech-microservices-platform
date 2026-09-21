package com.ecommerce.inventory.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.dto.inventory.StockCheckResponse;
import com.ecommerce.common.dto.inventory.StockReservationRequest;
import com.ecommerce.common.dto.inventory.StockReservationResponse;
import com.ecommerce.common.dto.inventory.StockUpdateRequest;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock Management, Concurrency Control, and Stock Reservations")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku}")
    @Operation(summary = "Get available stock for a product SKU")
    public ResponseEntity<ApiResponse<StockCheckResponse>> getStock(@PathVariable String sku) {
        StockCheckResponse response = inventoryService.checkStock(sku);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/check")
    @Operation(summary = "Check if requested quantity is available for SKU")
    public ResponseEntity<ApiResponse<StockCheckResponse>> checkAvailability(
            @RequestParam String sku,
            @RequestParam int quantity
    ) {
        StockCheckResponse response = inventoryService.checkAvailability(sku, quantity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Atomically reserve stock for one or more items")
    public ResponseEntity<ApiResponse<StockReservationResponse>> reserveStock(
            @Valid @RequestBody List<StockReservationRequest> items
    ) {
        StockReservationResponse response = inventoryService.reserveStock(items);
        return ResponseEntity.ok(ApiResponse.success("Stock reserved successfully", response));
    }

    @PostMapping("/release")
    @Operation(summary = "Release reserved stock back to available inventory")
    public ResponseEntity<ApiResponse<StockReservationResponse>> releaseStock(
            @Valid @RequestBody List<StockReservationRequest> items
    ) {
        StockReservationResponse response = inventoryService.releaseStock(items);
        return ResponseEntity.ok(ApiResponse.success("Stock released successfully", response));
    }

    @PutMapping("/stock")
    @Operation(summary = "Update stock level for SKU")
    public ResponseEntity<ApiResponse<StockCheckResponse>> updateStock(
            @Valid @RequestBody StockUpdateRequest request
    ) {
        StockCheckResponse response = inventoryService.updateStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", response));
    }

    @PostMapping
    @Operation(summary = "Create initial inventory record for new SKU")
    public ResponseEntity<ApiResponse<StockCheckResponse>> createInventory(
            @RequestParam String sku,
            @RequestParam(defaultValue = "0") int initialStock
    ) {
        StockCheckResponse response = inventoryService.createInventory(sku, initialStock);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory record created", response));
    }
}
