package com.foodapp.subscription.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Domain entity representing a customer's active or past meal subscription.
 */
public class Subscription {
    private int subscriptionId;
    private int customerId;
    private int planId;
    private String planName;
    private double agreedPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Subscription() {}

    public Subscription(int customerId, int planId, double agreedPrice, LocalDate startDate, LocalDate endDate) {
        this.customerId = customerId;
        this.planId = planId;
        this.agreedPrice = agreedPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public double getAgreedPrice() { return agreedPrice; }
    public void setAgreedPrice(double agreedPrice) { this.agreedPrice = agreedPrice; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActiveOn(LocalDate date) {
        if (status != SubscriptionStatus.ACTIVE) return false;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    @Override
    public String toString() {
        return "Subscription #" + subscriptionId + " [Plan #" + planId + (planName != null ? " - " + planName : "") + "] " +
                status + " (" + startDate + " to " + endDate + ") Rs." + agreedPrice;
    }
}
