package com.ecommerce.order;

import com.ecommerce.common.dto.cart.CartDto;
import com.ecommerce.common.dto.cart.CartItemDto;
import com.ecommerce.common.dto.order.CreateOrderRequest;
import com.ecommerce.common.dto.order.OrderDto;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.exception.InvalidOrderStateTransitionException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.entity.OrderEntity;
import com.ecommerce.order.entity.OrderItemEntity;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private CartClient cartClient;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity order;

    @BeforeEach
    void setUp() {
        OrderItemEntity item = OrderItemEntity.builder()
                .id(1L)
                .productId(3L)
                .sku("AUD-SONIC-NC")
                .productName("SonicWave Noise-Cancelling Headphones")
                .unitPrice(new BigDecimal("349.50"))
                .quantity(1)
                .subtotal(new BigDecimal("349.50"))
                .build();

        order = OrderEntity.builder()
                .id(100L)
                .orderNumber("ORD-20260901-0001")
                .userId(5L)
                .customerEmail("customer5@ecommerce.com")
                .shippingAddress("123 Main St")
                .totalAmount(new BigDecimal("349.50"))
                .status(OrderStatus.CONFIRMED)
                .items(new ArrayList<>(List.of(item)))
                .statusHistory(new ArrayList<>())
                .build();
        item.setOrder(order);
    }

    @Test
    @DisplayName("Create order snapshots product name and BigDecimal price immutably")
    void testCreateOrderSnapshotsPrice() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("456 Market St, City, Country")
                .build();

        CartItemDto cartItem = CartItemDto.builder()
                .id(1L)
                .productId(3L)
                .sku("AUD-SONIC-NC")
                .productName("SonicWave Noise-Cancelling Headphones")
                .unitPrice(new BigDecimal("349.50"))
                .quantity(2)
                .subtotal(new BigDecimal("699.00"))
                .build();

        CartDto cartDto = CartDto.builder()
                .id(1L)
                .userId(5L)
                .items(List.of(cartItem))
                .totalAmount(new BigDecimal("699.00"))
                .totalItems(2)
                .build();

        ProductDto product = ProductDto.builder()
                .id(3L)
                .sku("AUD-SONIC-NC")
                .name("SonicWave Noise-Cancelling Headphones")
                .price(new BigDecimal("349.50"))
                .build();

        when(cartClient.getCart(5L)).thenReturn(cartDto);
        when(productClient.getProductById(3L)).thenReturn(product);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto created = orderService.createOrder(5L, "customer5@ecommerce.com", request);

        assertNotNull(created);
        assertEquals(OrderStatus.CONFIRMED, created.getStatus());
        assertEquals(new BigDecimal("699.00"), created.getTotalAmount());
        assertEquals(1, created.getItems().size());
        assertEquals("SonicWave Noise-Cancelling Headphones", created.getItems().get(0).getProductName());
        assertEquals(new BigDecimal("349.50"), created.getItems().get(0).getUnitPrice());

        // Verify inventory reservation and cart clearance were invoked
        verify(inventoryClient, times(1)).reserveStock(any());
        verify(cartClient, times(1)).clearCart(5L);
    }

    @Test
    @DisplayName("Cancel order in CONFIRMED state succeeds and releases inventory")
    void testCancelOrderSuccess() {
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto cancelled = orderService.cancelOrder(100L, 5L, false);

        assertNotNull(cancelled);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        verify(inventoryClient, times(1)).releaseStock(any());
    }

    @Test
    @DisplayName("Cancel order in DELIVERED state throws InvalidOrderStateTransitionException")
    void testCancelDeliveredOrderThrows() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateTransitionException.class, () -> orderService.cancelOrder(100L, 5L, false));
        verify(inventoryClient, never()).releaseStock(any());
    }

    @Test
    @DisplayName("User accessing another user's order throws UnauthorizedException (IDOR defense)")
    void testGetOrderUnauthorizedThrows() {
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        // User 999 attempting to access Order belonging to user 5
        assertThrows(UnauthorizedException.class, () -> orderService.getOrderById(100L, 999L, false));
    }
}
