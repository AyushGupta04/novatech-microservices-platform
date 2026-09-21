package com.ecommerce.common.exception;

import com.ecommerce.common.enums.OrderStatus;

public class InvalidOrderStateTransitionException extends RuntimeException {
    public InvalidOrderStateTransitionException(String message) {
        super(message);
    }

    public InvalidOrderStateTransitionException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super(String.format("Cannot transition order from state '%s' to '%s'", currentStatus, targetStatus));
    }
}
