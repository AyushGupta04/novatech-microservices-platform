package com.ecommerce.common.enums;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING {
        @Override
        public Set<OrderStatus> nextAllowedStatuses() {
            return EnumSet.of(CONFIRMED, CANCELLED);
        }
    },
    CONFIRMED {
        @Override
        public Set<OrderStatus> nextAllowedStatuses() {
            return EnumSet.of(PROCESSING, CANCELLED);
        }
    },
    PROCESSING {
        @Override
        public Set<OrderStatus> nextAllowedStatuses() {
            return EnumSet.of(SHIPPED, CANCELLED);
        }
    },
    SHIPPED {
        @Override
        public Set<OrderStatus> nextAllowedStatuses() {
            return EnumSet.of(DELIVERED);
        }
    },
    DELIVERED {
        @Override
        public Set<OrderStatus> nextAllowedStatuses() {
            return EnumSet.noneOf(OrderStatus.class);
        }
    },
    CANCELLED {
        @Override
        public Set<OrderStatus> nextAllowedStatuses() {
            return EnumSet.noneOf(OrderStatus.class);
        }
    };

    public abstract Set<OrderStatus> nextAllowedStatuses();

    public boolean canTransitionTo(OrderStatus nextStatus) {
        return nextAllowedStatuses().contains(nextStatus);
    }

    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED || this == PROCESSING;
    }
}
