package com.foodapp.order.model;

import java.sql.Timestamp;

/**
 * Audit log entry capturing order status timeline transitions.
 */
public class OrderStatusHistory {
    private int historyId;
    private int orderId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String notes;
    private Timestamp changedAt;

    public OrderStatusHistory() {}

    public OrderStatusHistory(int orderId, OrderStatus oldStatus, OrderStatus newStatus, String notes) {
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.notes = notes;
    }

    public OrderStatusHistory(int historyId, int orderId, OrderStatus oldStatus, OrderStatus newStatus, String notes, Timestamp changedAt) {
        this.historyId = historyId;
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.notes = notes;
        this.changedAt = changedAt;
    }

    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public OrderStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(OrderStatus oldStatus) { this.oldStatus = oldStatus; }

    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getChangedAt() { return changedAt; }
    public void setChangedAt(Timestamp changedAt) { this.changedAt = changedAt; }

    @Override
    public String toString() {
        return (oldStatus != null ? oldStatus : "START") + " -> " + newStatus + " (" + notes + ") at " + changedAt;
    }
}
