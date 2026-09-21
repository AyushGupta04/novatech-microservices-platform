package com.ecommerce.cart;

import com.ecommerce.cart.client.InventoryClient;
import com.ecommerce.cart.client.ProductClient;
import com.ecommerce.cart.entity.CartEntity;
import com.ecommerce.cart.entity.CartItemEntity;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.common.dto.cart.AddToCartRequest;
import com.ecommerce.common.dto.cart.CartDto;
import com.ecommerce.common.dto.inventory.StockCheckResponse;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private CartService cartService;

    private CartEntity cart;
    private ProductDto product;

    @BeforeEach
    void setUp() {
        cart = CartEntity.builder()
                .id(1L)
                .userId(10L)
                .items(new ArrayList<>())
                .build();

        product = ProductDto.builder()
                .id(50L)
                .name("Noise Cancelling Headphones")
                .sku("AUD-SONIC-NC")
                .price(new BigDecimal("349.50"))
                .imageUrl("https://example.com/headphones.jpg")
                .build();
    }

    @Test
    @DisplayName("Get cart returns empty cart when user has no items")
    void testGetEmptyCart() {
        when(cartRepository.findByUserIdWithItems(10L)).thenReturn(Optional.of(cart));

        CartDto result = cartService.getCart(10L);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        assertEquals(0, result.getTotalItems());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
    }

    @Test
    @DisplayName("Add new item to cart successfully calculates subtotals")
    void testAddToCartSuccess() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(50L)
                .sku("AUD-SONIC-NC")
                .quantity(2)
                .build();

        when(cartRepository.findByUserIdWithItems(10L)).thenReturn(Optional.of(cart));
        when(productClient.getProductById(50L)).thenReturn(product);
        when(inventoryClient.checkAvailability("AUD-SONIC-NC", 2))
                .thenReturn(StockCheckResponse.builder().sku("AUD-SONIC-NC").inStock(true).availableQuantity(10).build());
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDto result = cartService.addToCart(10L, request);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getTotalItems());
        assertEquals(new BigDecimal("699.00"), result.getTotalAmount());
        assertEquals("Noise Cancelling Headphones", result.getItems().get(0).getProductName());
    }

    @Test
    @DisplayName("Add item throws InsufficientStockException when inventory is low")
    void testAddToCartInsufficientStock() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(50L)
                .sku("AUD-SONIC-NC")
                .quantity(20)
                .build();

        when(cartRepository.findByUserIdWithItems(10L)).thenReturn(Optional.of(cart));
        when(productClient.getProductById(50L)).thenReturn(product);
        when(inventoryClient.checkAvailability("AUD-SONIC-NC", 20))
                .thenReturn(StockCheckResponse.builder().sku("AUD-SONIC-NC").inStock(false).availableQuantity(5).build());

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(10L, request));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Clear cart removes all items")
    void testClearCart() {
        CartItemEntity item = CartItemEntity.builder()
                .id(99L)
                .cart(cart)
                .productId(50L)
                .sku("AUD-SONIC-NC")
                .productName("Headphones")
                .unitPrice(new BigDecimal("349.50"))
                .quantity(1)
                .build();
        cart.getItems().add(item);

        when(cartRepository.findByUserIdWithItems(10L)).thenReturn(Optional.of(cart));

        cartService.clearCart(10L);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
    }
}
