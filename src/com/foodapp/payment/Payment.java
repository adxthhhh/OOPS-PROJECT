package com.foodapp.payment;

import java.util.UUID;

/** Abstract base class for all payment methods. */
public abstract class Payment {
    protected double amount;
    protected String status = "PENDING";   // PENDING, SUCCESS, FAILED
    protected String transactionId = "";
    protected String message = "";         // success / failure reason

    public Payment(double amount) {
        this.amount = amount;
    }

    /** Each payment type validates its own details and processes payment. */
    public abstract boolean pay();

    public abstract String getMethodName();

    protected boolean markSuccess(String msg) {
        status = "SUCCESS";
        transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        message = msg;
        return true;
    }

    protected boolean markFailed(String reason) {
        status = "FAILED";
        message = reason;
        return false;
    }

    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
    public String getMessage() { return message; }
}
