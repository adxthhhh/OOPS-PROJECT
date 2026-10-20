package com.foodapp.payment;

public class CODPayment extends Payment {
    private String address;

    public CODPayment(double amount, String address) {
        super(amount);
        this.address = address == null ? "" : address.trim();
    }

    @Override
    public boolean pay() {
        if (address.isEmpty()) return markFailed("Delivery address is required for COD.");
        return markSuccess("Order confirmed. Pay Rs." + amount + " in cash on delivery.");
    }

    @Override
    public String getMethodName() { return "Cash on Delivery"; }
}
