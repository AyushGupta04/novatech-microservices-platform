package com.ecommerce.product;

import com.ecommerce.common.dto.product.ProductCreateRequest;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.dto.product.ProductUpdateRequest;
import com.ecommerce.common.exception.DuplicateResourceException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.entity.CategoryEntity;
import com.ecommerce.product.entity.ProductEntity;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private CategoryEntity category;
    private ProductEntity product;

    @BeforeEach
    void setUp() {
        category = CategoryEntity.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .build();

        product = ProductEntity.builder()
                .id(100L)
                .name("Test Laptop")
                .description("High end test laptop")
                .price(new BigDecimal("1299.99"))
                .sku("TEST-LAPTOP-1")
                .category(category)
                .active(true)
                .stockQuantity(25)
                .build();
    }

    @Test
    @DisplayName("Create product successfully with unique SKU")
    void testCreateProductSuccess() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Test Laptop")
                .description("High end test laptop")
                .price(new BigDecimal("1299.99"))
                .sku("TEST-LAPTOP-1")
                .categoryId(1L)
                .initialStock(25)
                .build();

        when(productRepository.existsBySku("TEST-LAPTOP-1")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(product);

        ProductDto result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("Test Laptop", result.getName());
        assertEquals("TEST-LAPTOP-1", result.getSku());
        assertEquals(new BigDecimal("1299.99"), result.getPrice());
        assertEquals(25, result.getStockQuantity());
    }

    @Test
    @DisplayName("Create product fails when SKU already exists")
    void testCreateProductDuplicateSku() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Test Laptop")
                .price(new BigDecimal("1299.99"))
                .sku("EXISTING-SKU")
                .categoryId(1L)
                .build();

        when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get product by ID returns mapped DTO")
    void testGetProductById() {
        when(productRepository.findByIdWithCategory(100L)).thenReturn(Optional.of(product));

        ProductDto result = productService.getProductById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Electronics", result.getCategoryName());
    }

    @Test
    @DisplayName("Get product by nonexistent ID throws ResourceNotFoundException")
    void testGetProductNotFound() {
        when(productRepository.findByIdWithCategory(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    @DisplayName("Search products returns paginated results")
    void testSearchProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductEntity> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.searchProducts(null, null, null, "laptop", true, pageable)).thenReturn(page);

        Page<ProductDto> result = productService.getProducts(null, null, null, "laptop", true, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Laptop", result.getContent().get(0).getName());
    }
}
