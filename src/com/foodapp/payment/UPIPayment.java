package com.foodapp.payment;

public class UPIPayment extends Payment {
    private String upiId;

    public UPIPayment(double amount, String upiId) {
        super(amount);
        this.upiId = upiId == null ? "" : upiId.trim();
    }

    @Override
    public boolean pay() {
        if (!upiId.matches("^[\\w.\\-]{2,}@[a-zA-Z]{2,}$"))
            return markFailed("Invalid UPI ID. Use format name@bank.");
        return markSuccess("Paid Rs." + amount + " using UPI (" + upiId + ").");
    }

    @Override
    public String getMethodName() { return "UPI"; }
}
