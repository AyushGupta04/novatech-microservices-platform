package com.ecommerce.common;

import com.ecommerce.common.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    @DisplayName("Verify valid forward state transitions")
    void testValidForwardTransitions() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.SHIPPED));
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    @DisplayName("Verify valid cancellation transitions")
    void testValidCancellations() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CANCELLED));

        assertTrue(OrderStatus.PENDING.isCancellable());
        assertTrue(OrderStatus.CONFIRMED.isCancellable());
        assertTrue(OrderStatus.PROCESSING.isCancellable());
    }

    @Test
    @DisplayName("Verify forbidden cancellations from shipped or delivered state")
    void testForbiddenCancellations() {
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CANCELLED));

        assertFalse(OrderStatus.SHIPPED.isCancellable());
        assertFalse(OrderStatus.DELIVERED.isCancellable());
        assertFalse(OrderStatus.CANCELLED.isCancellable());
    }

    @Test
    @DisplayName("Verify backward transitions are forbidden")
    void testForbiddenBackwardTransitions() {
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.PROCESSING));
        assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CONFIRMED));
    }
}
