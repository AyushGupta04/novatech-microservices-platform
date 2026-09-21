package com.ecommerce.cart.controller;

import com.ecommerce.cart.service.CartService;
import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.dto.cart.AddToCartRequest;
import com.ecommerce.common.dto.cart.CartDto;
import com.ecommerce.common.dto.cart.UpdateCartItemRequest;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "User Shopping Cart Operations")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get the current authenticated user's cart")
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) {
        Long userId = resolveUserId(principal, request);
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the current user's cart")
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToCartRequest addItemRequest,
            HttpServletRequest request
    ) {
        Long userId = resolveUserId(principal, request);
        CartDto cart = cartService.addToCart(userId, addItemRequest);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update quantity of an item in the cart")
    public ResponseEntity<ApiResponse<CartDto>> updateQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest updateRequest,
            HttpServletRequest request
    ) {
        Long userId = resolveUserId(principal, request);
        CartDto cart = cartService.updateQuantity(userId, itemId, updateRequest.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId,
            HttpServletRequest request
    ) {
        Long userId = resolveUserId(principal, request);
        CartDto cart = cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear all items from the cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) {
        Long userId = resolveUserId(principal, request);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    private Long resolveUserId(UserPrincipal principal, HttpServletRequest request) {
        if (principal != null && principal.id() != null) {
            return principal.id();
        }
        String headerUserId = request.getHeader(SecurityConstants.HEADER_USER_ID);
        if (headerUserId != null && !headerUserId.isBlank()) {
            return Long.parseLong(headerUserId);
        }
        throw new UnauthorizedException("Authentication required to access shopping cart");
    }
}
