package com.foodapp.order.model;

/**
 * Lifecycle states of an order in MealFlow.
 * Permitted normal progression:
 * PLACED -> CONFIRMED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
 * Orders may transition to CANCELLED only before OUT_FOR_DELIVERY.
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (this == next) return true;
        switch (this) {
            case PLACED:
                return next == CONFIRMED || next == CANCELLED;
            case CONFIRMED:
                return next == PREPARING || next == CANCELLED;
            case PREPARING:
                return next == OUT_FOR_DELIVERY || next == CANCELLED;
            case OUT_FOR_DELIVERY:
                return next == DELIVERED;
            case DELIVERED:
            case CANCELLED:
            default:
                return false; // Terminal states
        }
    }
}
