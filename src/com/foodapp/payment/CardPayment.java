package com.foodapp.payment;

import java.time.YearMonth;

public class CardPayment extends Payment {
    private String cardNumber, holderName, expiry, cvv;

    public CardPayment(double amount, String cardNumber, String holderName, String expiry, String cvv) {
        super(amount);
        this.cardNumber = cardNumber == null ? "" : cardNumber.replaceAll("\\s", "");
        this.holderName = holderName == null ? "" : holderName.trim();
        this.expiry = expiry == null ? "" : expiry.trim();
        this.cvv = cvv == null ? "" : cvv.trim();
    }

    private boolean expiryValid() {
        if (!expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$")) return false;
        int month = Integer.parseInt(expiry.substring(0, 2));
        int year = 2000 + Integer.parseInt(expiry.substring(3));
        return !YearMonth.of(year, month).isBefore(YearMonth.now());
    }

    @Override
    public boolean pay() {
        if (holderName.isEmpty()) return markFailed("Card holder name is required.");
        if (!cardNumber.matches("\\d{16}")) return markFailed("Card number must be 16 digits.");
        if (!expiryValid()) return markFailed("Expiry must be MM/YY and not in the past.");
        if (!cvv.matches("\\d{3}")) return markFailed("CVV must be 3 digits.");
        String last4 = cardNumber.substring(12);
        return markSuccess("Paid Rs." + amount + " using card ending " + last4 + ".");
    }

    @Override
    public String getMethodName() { return "Card"; }
}
