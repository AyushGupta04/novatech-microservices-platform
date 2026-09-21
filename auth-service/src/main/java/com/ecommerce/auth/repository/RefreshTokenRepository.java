package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.RefreshTokenEntity;
import com.ecommerce.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    void deleteByUser(UserEntity user);
}
