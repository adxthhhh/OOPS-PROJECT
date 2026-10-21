package com.foodapp.order.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a customer order in MealFlow.
 */
public class Order {
    private int orderId;
    private int customerId;
    private OrderStatus orderStatus = OrderStatus.PLACED;
    private double subtotal;
    private double deliveryFee;
    private double totalAmount;
    private String deliveryAddress;
    private String contactPhone;
    private String estimatedDeliveryTime = "30-40 mins";
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(int customerId, double subtotal, double deliveryFee, double totalAmount,
                 String deliveryAddress, String contactPhone) {
        this.customerId = customerId;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.contactPhone = contactPhone;
        this.orderStatus = OrderStatus.PLACED;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = (items != null) ? items : new ArrayList<>(); }
    public void addItem(OrderItem item) { this.items.add(item); }

    @Override
    public String toString() {
        return "Order #" + orderId + " [" + orderStatus + "] Total: Rs." + totalAmount + " (" + items.size() + " items)";
    }
}
