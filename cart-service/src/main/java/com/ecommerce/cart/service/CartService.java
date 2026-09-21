package com.ecommerce.cart.service;

import com.ecommerce.cart.client.InventoryClient;
import com.ecommerce.cart.client.ProductClient;
import com.ecommerce.cart.entity.CartEntity;
import com.ecommerce.cart.entity.CartItemEntity;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.common.dto.cart.AddToCartRequest;
import com.ecommerce.common.dto.cart.CartDto;
import com.ecommerce.common.dto.cart.CartItemDto;
import com.ecommerce.common.dto.inventory.StockCheckResponse;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.exception.InsufficientStockException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Transactional
    public CartDto getCart(Long userId) {
        CartEntity cart = getOrCreateCart(userId);
        return mapToDto(cart);
    }

    @Transactional
    public CartDto addToCart(Long userId, AddToCartRequest request) {
        CartEntity cart = getOrCreateCart(userId);

        // Fetch authoritative product info from Product Service
        ProductDto product = productClient.getProductById(request.getProductId());
        String sku = (request.getSku() != null && !request.getSku().isBlank()) ? request.getSku() : product.getSku();

        // Check stock availability from Inventory Service
        int existingQty = cart.getItems().stream()
                .filter(item -> item.getSku().equalsIgnoreCase(sku))
                .mapToInt(CartItemEntity::getQuantity)
                .findFirst()
                .orElse(0);

        int totalRequestedQty = existingQty + request.getQuantity();
        StockCheckResponse stock = inventoryClient.checkAvailability(sku, totalRequestedQty);
        if (!stock.isInStock()) {
            throw new InsufficientStockException(sku, totalRequestedQty, stock.getAvailableQuantity());
        }

        Optional<CartItemEntity> existingItem = cart.getItems().stream()
                .filter(item -> item.getSku().equalsIgnoreCase(sku))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            item.setQuantity(totalRequestedQty);
            item.setUnitPrice(product.getPrice()); // update to current price
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .sku(product.getSku())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(request.getQuantity())
                    .imageUrl(product.getImageUrl())
                    .build();
            cart.getItems().add(newItem);
        }

        CartEntity savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Transactional
    public CartDto updateQuantity(Long userId, Long itemId, int quantity) {
        CartEntity cart = getOrCreateCart(userId);

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            // Check inventory
            StockCheckResponse stock = inventoryClient.checkAvailability(item.getSku(), quantity);
            if (!stock.isInStock()) {
                throw new InsufficientStockException(item.getSku(), quantity, stock.getAvailableQuantity());
            }
            item.setQuantity(quantity);
        }

        CartEntity savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Transactional
    public CartDto removeItem(Long userId, Long itemId) {
        CartEntity cart = getOrCreateCart(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item", "id", itemId);
        }

        CartEntity savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Transactional
    public void clearCart(Long userId) {
        CartEntity cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cleared cart for user ID: {}", userId);
    }

    private CartEntity getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> cartRepository.save(CartEntity.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build()));
    }

    public CartDto mapToDto(CartEntity entity) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        var itemsDto = new ArrayList<CartItemDto>();
        for (CartItemEntity item : entity.getItems()) {
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            totalItems += item.getQuantity();

            itemsDto.add(CartItemDto.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .sku(item.getSku())
                    .productName(item.getProductName())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .imageUrl(item.getImageUrl())
                    .build());
        }

        return CartDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .items(itemsDto)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }
}
