package com.ecommerce.inventory;

import com.ecommerce.common.dto.inventory.StockReservationRequest;
import com.ecommerce.common.exception.InsufficientStockException;
import com.ecommerce.inventory.entity.InventoryEntity;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InventoryConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    @DisplayName("Verify concurrent stock reservations prevent negative inventory under heavy concurrent load")
    void testConcurrentReservationsPreventNegativeStock() throws InterruptedException {
        String testSku = "CONCUR-SKU-100";
        int initialStock = 10;
        int threadCount = 25; // 25 concurrent requests competing for 10 units

        // Seed fresh inventory item
        inventoryRepository.save(InventoryEntity.builder()
                .sku(testSku)
                .quantity(initialStock)
                .reservedQuantity(0)
                .build());

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to start simultaneously
                    inventoryService.reserveStock(List.of(
                            StockReservationRequest.builder().sku(testSku).quantity(1).build()
                    ));
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    // unexpected exception
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Fire all threads simultaneously
        startLatch.countDown();
        boolean completed = endLatch.await(15, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(completed, "All concurrent reservation tasks should complete within 15 seconds");

        // Fetch final state from database
        InventoryEntity finalInventory = inventoryRepository.findBySku(testSku).orElseThrow();

        System.out.println("=================================================================");
        System.out.println("CONCURRENCY TEST RESULTS:");
        System.out.printf("  Initial Stock        : %d%n", initialStock);
        System.out.printf("  Total Competitors    : %d%n", threadCount);
        System.out.printf("  Successful Reserves  : %d%n", successCount.get());
        System.out.printf("  Failed Reserves      : %d%n", failureCount.get());
        System.out.printf("  Final Remaining Stock: %d%n", finalInventory.getQuantity());
        System.out.printf("  Final Reserved Stock : %d%n", finalInventory.getReservedQuantity());
        System.out.println("=================================================================");

        // Verifications
        assertEquals(initialStock, successCount.get(), "Exactly the available initial stock should be successfully reserved");
        assertEquals(threadCount - initialStock, failureCount.get(), "Remaining attempts must fail due to stock depletion");
        assertEquals(0, finalInventory.getQuantity(), "Final stock must not drop below zero");
        assertTrue(finalInventory.getQuantity() >= 0, "Stock must never be negative!");
        assertEquals(initialStock, finalInventory.getReservedQuantity(), "Reserved quantity must match initial stock");
    }
}
