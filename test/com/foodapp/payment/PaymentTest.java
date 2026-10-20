package com.foodapp.payment;

public class PaymentTest {
    static void run(Payment p) {
        System.out.println(p.getMethodName() + " -> " + p.pay() + " | " + p.getStatus() + " | " + p.getMessage());
    }
    public static void main(String[] a) {
        run(new UPIPayment(500, "ravi@okbank"));
        run(new UPIPayment(500, "ravi"));
        run(new CardPayment(500, "1234 5678 9012 3456", "Ravi", "12/30", "123"));
        run(new CardPayment(500, "1234", "Ravi", "12/30", "123"));
        run(new WalletPayment(500, "Paytm", 1000));
        run(new WalletPayment(500, "Paytm", 100));
        run(new CODPayment(500, "MG Road, Kochi"));
        run(new CODPayment(500, ""));
    }
}
