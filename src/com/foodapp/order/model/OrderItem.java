package com.foodapp.order.model;

/**
 * Represents a line item inside an order, preserving immutable snapshots of price and name at purchase time.
 */
public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int itemId;
    private String itemNameSnapshot;
    private int quantity;
    private double unitPriceSnapshot;
    private double subtotal;

    public OrderItem() {}

    public OrderItem(int itemId, String itemNameSnapshot, int quantity, double unitPriceSnapshot) {
        this.itemId = itemId;
        this.itemNameSnapshot = itemNameSnapshot;
        this.quantity = quantity;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.subtotal = unitPriceSnapshot * quantity;
    }

    public OrderItem(int orderItemId, int orderId, int itemId, String itemNameSnapshot, int quantity, double unitPriceSnapshot, double subtotal) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemNameSnapshot = itemNameSnapshot;
        this.quantity = quantity;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.subtotal = subtotal;
    }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemNameSnapshot() { return itemNameSnapshot; }
    public void setItemNameSnapshot(String itemNameSnapshot) { this.itemNameSnapshot = itemNameSnapshot; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = this.unitPriceSnapshot * quantity;
    }

    public double getUnitPriceSnapshot() { return unitPriceSnapshot; }
    public void setUnitPriceSnapshot(double unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.subtotal = unitPriceSnapshot * this.quantity;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    @Override
    public String toString() {
        return itemNameSnapshot + " x " + quantity + " @ Rs." + unitPriceSnapshot + " = Rs." + subtotal;
    }
}
