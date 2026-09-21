package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.dto.order.CreateOrderRequest;
import com.ecommerce.common.dto.order.OrderDto;
import com.ecommerce.common.dto.order.UpdateOrderStatusRequest;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.common.security.UserPrincipal;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order Placement, Tracking, State Transitions, and Administration")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order (from active cart or explicit items)")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(principal, httpRequest);
        String email = resolveUserEmail(principal, httpRequest);

        OrderDto order = orderService.createOrder(userId, email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get paginated order history for the authenticated user")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getUserOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(principal, httpRequest);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID (enforcing user ownership or admin role)")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(principal, httpRequest);
        boolean isAdmin = principal != null && principal.hasRole("ROLE_ADMIN");
        OrderDto order = orderService.getOrderById(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order (only allowed in PENDING, CONFIRMED, or PROCESSING states)")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(principal, httpRequest);
        boolean isAdmin = principal != null && principal.hasRole("ROLE_ADMIN");
        OrderDto order = orderService.cancelOrder(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all platform orders (Admin only)")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getAllOrdersAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDto> orders = orderService.getAllOrdersAdmin(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Transition order status (Admin only, validates state machine rules)")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatusAdmin(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestParam(required = false) String notes
    ) {
        OrderDto order = orderService.updateOrderStatusAdmin(id, request.getStatus(), notes);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    private Long resolveUserId(UserPrincipal principal, HttpServletRequest request) {
        if (principal != null && principal.id() != null) {
            return principal.id();
        }
        String headerUserId = request.getHeader(SecurityConstants.HEADER_USER_ID);
        if (headerUserId != null && !headerUserId.isBlank()) {
            return Long.parseLong(headerUserId);
        }
        throw new UnauthorizedException("Authentication required to manage orders");
    }

    private String resolveUserEmail(UserPrincipal principal, HttpServletRequest request) {
        if (principal != null && principal.email() != null) {
            return principal.email();
        }
        String headerEmail = request.getHeader(SecurityConstants.HEADER_USER_EMAIL);
        if (headerEmail != null && !headerEmail.isBlank()) {
            return headerEmail;
        }
        return "customer@ecommerce.com";
    }
}
