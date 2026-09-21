package com.ecommerce.order.service;

import com.ecommerce.common.dto.cart.CartDto;
import com.ecommerce.common.dto.cart.CartItemDto;
import com.ecommerce.common.dto.inventory.StockReservationRequest;
import com.ecommerce.common.dto.order.CreateOrderRequest;
import com.ecommerce.common.dto.order.OrderDto;
import com.ecommerce.common.dto.order.OrderItemDto;
import com.ecommerce.common.dto.order.OrderItemRequest;
import com.ecommerce.common.dto.product.ProductDto;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.exception.InvalidOrderStateTransitionException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.entity.OrderEntity;
import com.ecommerce.order.entity.OrderItemEntity;
import com.ecommerce.order.entity.OrderStatusHistoryEntity;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CartClient cartClient;

    @Transactional
    public OrderDto createOrder(Long userId, String customerEmail, CreateOrderRequest request) {
        List<OrderItemEntity> orderItems = new ArrayList<>();
        List<StockReservationRequest> stockReservations = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // If specific items were passed in request, use them; otherwise pull from user's active cart
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderItemRequest itemReq : request.getItems()) {
                ProductDto product = productClient.getProductById(itemReq.getProductId());
                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                totalAmount = totalAmount.add(subtotal);

                orderItems.add(OrderItemEntity.builder()
                        .productId(product.getId())
                        .sku(product.getSku())
                        .productName(product.getName()) // Permanently snapshotted
                        .unitPrice(product.getPrice())  // Permanently snapshotted
                        .quantity(itemReq.getQuantity())
                        .subtotal(subtotal)
                        .build());

                stockReservations.add(StockReservationRequest.builder()
                        .sku(product.getSku())
                        .quantity(itemReq.getQuantity())
                        .build());
            }
        } else {
            CartDto cart = cartClient.getCart(userId);
            if (cart == null || cart.getItems().isEmpty()) {
                throw new IllegalArgumentException("Cannot place order: Cart is empty and no items were specified");
            }

            for (CartItemDto cartItem : cart.getItems()) {
                ProductDto product = productClient.getProductById(cartItem.getProductId());
                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                totalAmount = totalAmount.add(subtotal);

                orderItems.add(OrderItemEntity.builder()
                        .productId(product.getId())
                        .sku(product.getSku())
                        .productName(product.getName()) // Permanently snapshotted
                        .unitPrice(product.getPrice())  // Permanently snapshotted
                        .quantity(cartItem.getQuantity())
                        .subtotal(subtotal)
                        .build());

                stockReservations.add(StockReservationRequest.builder()
                        .sku(product.getSku())
                        .quantity(cartItem.getQuantity())
                        .build());
            }
        }

        // 1. Atomically reserve inventory
        inventoryClient.reserveStock(stockReservations);

        // 2. Generate unique order number: ORD-YYYYMMDD-XXXXXX
        String orderNumber = generateOrderNumber();

        // 3. Build and persist order
        OrderEntity order = OrderEntity.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .customerEmail(customerEmail)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(totalAmount)
                .status(OrderStatus.CONFIRMED)
                .build();

        for (OrderItemEntity item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);

        OrderStatusHistoryEntity initialHistory = OrderStatusHistoryEntity.builder()
                .order(order)
                .fromStatus(OrderStatus.PENDING)
                .toStatus(OrderStatus.CONFIRMED)
                .notes("Order placed and inventory reserved")
                .build();
        order.getStatusHistory().add(initialHistory);

        OrderEntity savedOrder = orderRepository.save(order);

        // 4. Clear user's cart asynchronously or via client
        cartClient.clearCart(userId);

        log.info("Successfully created order {} for user {}", savedOrder.getOrderNumber(), userId);
        return mapToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId, Long userId, boolean isAdmin) {
        OrderEntity order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Ownership Security Check: Users can only access their own orders!
        if (!isAdmin && !order.getUserId().equals(userId)) {
            log.warn("IDOR attempt: User {} attempted to access Order {} belonging to User {}", userId, orderId, order.getUserId());
            throw new UnauthorizedException("You do not have permission to view this order");
        }

        return mapToDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId, Long userId, boolean isAdmin) {
        OrderEntity order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to cancel this order");
        }

        OrderStatus currentStatus = order.getStatus();
        if (!currentStatus.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new InvalidOrderStateTransitionException(currentStatus, OrderStatus.CANCELLED);
        }

        // Release reserved stock back to inventory
        List<StockReservationRequest> releaseItems = order.getItems().stream()
                .map(item -> StockReservationRequest.builder()
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        inventoryClient.releaseStock(releaseItems);

        order.setStatus(OrderStatus.CANCELLED);

        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.builder()
                .order(order)
                .fromStatus(currentStatus)
                .toStatus(OrderStatus.CANCELLED)
                .notes("Order cancelled by " + (isAdmin ? "Admin" : "Customer"))
                .build();
        order.getStatusHistory().add(history);

        OrderEntity saved = orderRepository.save(order);
        log.info("Order {} cancelled successfully", order.getOrderNumber());
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrdersAdmin(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public OrderDto updateOrderStatusAdmin(Long orderId, OrderStatus newStatus, String notes) {
        OrderEntity order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus currentStatus = order.getStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateTransitionException(currentStatus, newStatus);
        }

        // If transitioning to CANCELLED, release stock
        if (newStatus == OrderStatus.CANCELLED) {
            List<StockReservationRequest> releaseItems = order.getItems().stream()
                    .map(item -> StockReservationRequest.builder()
                            .sku(item.getSku())
                            .quantity(item.getQuantity())
                            .build())
                    .collect(Collectors.toList());
            inventoryClient.releaseStock(releaseItems);
        }

        order.setStatus(newStatus);

        OrderStatusHistoryEntity history = OrderStatusHistoryEntity.builder()
                .order(order)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .notes(notes != null ? notes : "Status updated to " + newStatus)
                .build();
        order.getStatusHistory().add(history);

        OrderEntity saved = orderRepository.save(order);
        log.info("Order {} transitioned from {} to {}", order.getOrderNumber(), currentStatus, newStatus);
        return mapToDto(saved);
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + datePrefix + "-" + randomSuffix;
    }

    public OrderDto mapToDto(OrderEntity entity) {
        List<OrderItemDto> itemDtos = entity.getItems() == null ? List.of() :
                entity.getItems().stream()
                        .map(i -> OrderItemDto.builder()
                                .id(i.getId())
                                .productId(i.getProductId())
                                .sku(i.getSku())
                                .productName(i.getProductName())
                                .unitPrice(i.getUnitPrice())
                                .quantity(i.getQuantity())
                                .subtotal(i.getSubtotal())
                                .build())
                        .collect(Collectors.toList());

        return OrderDto.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .userId(entity.getUserId())
                .customerEmail(entity.getCustomerEmail())
                .shippingAddress(entity.getShippingAddress())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .items(itemDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
