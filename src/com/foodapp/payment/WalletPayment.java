package com.foodapp.payment;

public class WalletPayment extends Payment {
    private String walletName;
    private double walletBalance;

    public WalletPayment(double amount, String walletName, double walletBalance) {
        super(amount);
        this.walletName = walletName;
        this.walletBalance = walletBalance;
    }

    @Override
    public boolean pay() {
        if (walletBalance < amount)
            return markFailed("Insufficient wallet balance (Rs." + walletBalance + ").");
        walletBalance -= amount;
        return markSuccess("Paid Rs." + amount + " from " + walletName
                + ". Balance left: Rs." + walletBalance);
    }

    public double getWalletBalance() { return walletBalance; }

    @Override
    public String getMethodName() { return "Wallet"; }
}
