package com.ecommerce.product.service;

import com.ecommerce.common.dto.product.ProductCreateRequest;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.dto.product.ProductUpdateRequest;
import com.ecommerce.common.exception.DuplicateResourceException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.config.RedisConfig;
import com.ecommerce.product.entity.CategoryEntity;
import com.ecommerce.product.entity.ProductEntity;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            Boolean active,
            Pageable pageable
    ) {
        return productRepository.searchProducts(categoryId, minPrice, maxPrice, search, active, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_PRODUCTS, key = "#id")
    public ProductDto getProductById(Long id) {
        ProductEntity product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        ProductEntity product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return mapToDto(product);
    }

    @Transactional
    @CacheEvict(value = {RedisConfig.CACHE_PRODUCTS, RedisConfig.CACHE_CATEGORIES}, allEntries = true)
    public ProductDto createProduct(ProductCreateRequest request) {
        String normalizedSku = request.getSku().trim().toUpperCase();
        if (productRepository.existsBySku(normalizedSku)) {
            throw new DuplicateResourceException("Product with SKU '" + normalizedSku + "' already exists");
        }

        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        ProductEntity product = ProductEntity.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .sku(normalizedSku)
                .category(category)
                .imageUrl(request.getImageUrl())
                .active(true)
                .stockQuantity(request.getInitialStock() != null ? request.getInitialStock() : 0)
                .build();

        ProductEntity saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional
    @CacheEvict(value = {RedisConfig.CACHE_PRODUCTS, RedisConfig.CACHE_CATEGORIES}, allEntries = true)
    public ProductDto updateProduct(Long id, ProductUpdateRequest request) {
        ProductEntity product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        ProductEntity updated = productRepository.save(product);
        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = {RedisConfig.CACHE_PRODUCTS, RedisConfig.CACHE_CATEGORIES}, allEntries = true)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_PRODUCTS, key = "#result.id", condition = "#result != null")
    public ProductDto updateStock(String sku, int newQuantity) {
        ProductEntity product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));

        product.setStockQuantity(newQuantity);
        ProductEntity updated = productRepository.save(product);
        return mapToDto(updated);
    }

    public ProductDto mapToDto(ProductEntity entity) {
        return ProductDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .sku(entity.getSku())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .imageUrl(entity.getImageUrl())
                .active(entity.isActive())
                .stockQuantity(entity.getStockQuantity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
