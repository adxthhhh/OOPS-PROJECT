package com.foodapp.payment;

import java.sql.Timestamp;

/**
 * Persistent representation of a payment transaction stored in MySQL.
 */
public class PaymentRecord {
    private int paymentId;
    private int orderId;
    private String paymentMethod; // UPI, CARD, WALLET, COD
    private double amount;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String transactionRef;
    private String paymentDetails;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public PaymentRecord() {}

    public PaymentRecord(int orderId, String paymentMethod, double amount, PaymentStatus paymentStatus, String transactionRef, String paymentDetails) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.transactionRef = transactionRef;
        this.paymentDetails = paymentDetails;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "PaymentRecord #" + paymentId + " for Order #" + orderId + " [" + paymentMethod + "] " + paymentStatus + " Rs." + amount + " (" + transactionRef + ")";
    }
}
