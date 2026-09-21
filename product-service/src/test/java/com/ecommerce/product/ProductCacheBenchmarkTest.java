package com.ecommerce.product;

import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ProductCacheBenchmarkTest {

    private static final Logger log = LoggerFactory.getLogger(ProductCacheBenchmarkTest.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Measure latency difference between cold database queries and warm cache lookups")
    void benchmarkCacheVsDatabase() {
        int iterations = 100;

        // 1. Direct Database Queries (bypassing cache)
        long dbStartTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            var entity = productRepository.findByIdWithCategory(1L);
            assertNotNull(entity.orElse(null));
        }
        long dbTotalNanos = System.nanoTime() - dbStartTime;
        double dbAvgMicros = (double) dbTotalNanos / iterations / 1_000.0;

        // Warm up cache with initial call
        ProductDto initial = productService.getProductById(1L);
        assertNotNull(initial);

        // 2. Cached Service Lookups
        long cacheStartTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ProductDto cached = productService.getProductById(1L);
            assertNotNull(cached);
        }
        long cacheTotalNanos = System.nanoTime() - cacheStartTime;
        double cacheAvgMicros = (double) cacheTotalNanos / iterations / 1_000.0;

        double speedupRatio = (double) dbTotalNanos / cacheTotalNanos;

        System.out.println("=================================================================");
        System.out.printf("BENCHMARK RESULTS (%d iterations):%n", iterations);
        System.out.printf("  Direct Database Query Average Latency : %.3f µs (%.3f ms)%n", dbAvgMicros, dbAvgMicros / 1000.0);
        System.out.printf("  Cached Lookup Average Latency         : %.3f µs (%.3f ms)%n", cacheAvgMicros, cacheAvgMicros / 1000.0);
        System.out.printf("  Measured Cache Speedup Factor         : %.2fx faster%n", speedupRatio);
        System.out.println("=================================================================");

        // Verifiable assertion: Cache lookup should be faster than direct database query
        assertTrue(cacheAvgMicros < dbAvgMicros,
                String.format("Expected cached lookup (%.3f µs) to be faster than database lookup (%.3f µs)", cacheAvgMicros, dbAvgMicros));
    }
}
