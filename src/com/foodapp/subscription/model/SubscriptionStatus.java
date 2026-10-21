package com.foodapp.subscription.model;

/**
 * Status lifecycle of a customer subscription in MealFlow.
 */
public enum SubscriptionStatus {
    ACTIVE,
    PAUSED,
    CANCELLED,
    EXPIRED;

    public boolean canPause() {
        return this == ACTIVE;
    }

    public boolean canResume() {
        return this == PAUSED;
    }

    public boolean canCancel() {
        return this == ACTIVE || this == PAUSED;
    }
}
