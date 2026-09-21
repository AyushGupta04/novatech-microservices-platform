package com.ecommerce.product.repository;

import com.ecommerce.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM ProductEntity p JOIN FETCH p.category WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithCategory(@Param("id") Long id);

    @Query(
        value = "SELECT p FROM ProductEntity p JOIN FETCH p.category c " +
                "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
                "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
                "     OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                "AND (:active IS NULL OR p.active = :active)",
        countQuery = "SELECT count(p) FROM ProductEntity p " +
                     "WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
                     "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                     "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                     "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                     "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
                     "     OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                     "AND (:active IS NULL OR p.active = :active)"
    )
    Page<ProductEntity> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
