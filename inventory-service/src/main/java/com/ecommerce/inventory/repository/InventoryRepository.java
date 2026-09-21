package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    Optional<InventoryEntity> findBySku(String sku);

    boolean existsBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.sku = :sku")
    Optional<InventoryEntity> findBySkuForUpdate(@Param("sku") String sku);

    @Modifying
    @Query("UPDATE InventoryEntity i SET i.quantity = i.quantity - :quantity, " +
           "i.reservedQuantity = i.reservedQuantity + :quantity " +
           "WHERE i.sku = :sku AND i.quantity >= :quantity")
    int reserveStockAtomic(@Param("sku") String sku, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE InventoryEntity i SET i.quantity = i.quantity + :quantity, " +
           "i.reservedQuantity = CASE WHEN i.reservedQuantity >= :quantity THEN i.reservedQuantity - :quantity ELSE 0 END " +
           "WHERE i.sku = :sku")
    int releaseStock(@Param("sku") String sku, @Param("quantity") int quantity);
}
