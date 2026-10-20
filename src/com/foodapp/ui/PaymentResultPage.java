package com.foodapp.ui;

import com.foodapp.payment.Payment;

import javax.swing.*;
import java.awt.*;

/** Shows payment success or failure. */
public class PaymentResultPage extends JFrame {
    public PaymentResultPage(Payment payment) {
        boolean ok = payment.getStatus().equals("SUCCESS");
        setTitle(ok ? "Payment Successful" : "Payment Failed");
        setSize(420, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel head = new JLabel(ok ? "Payment Successful" : "Payment Failed", SwingConstants.CENTER);
        head.setFont(new Font("SansSerif", Font.BOLD, 22));
        head.setForeground(ok ? new Color(0, 140, 60) : Color.RED);
        add(head, BorderLayout.NORTH);

        StringBuilder sb = new StringBuilder();
        sb.append("Method: ").append(payment.getMethodName()).append("\n");
        sb.append("Amount: Rs.").append(payment.getAmount()).append("\n");
        if (ok) sb.append("Transaction ID: ").append(payment.getTransactionId()).append("\n");
        sb.append("\n").append(payment.getMessage());
        JTextArea info = new JTextArea(sb.toString());
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setMargin(new Insets(10, 20, 10, 20));
        add(info, BorderLayout.CENTER);

        JButton btn = new JButton(ok ? "Done" : "Try Again");
        btn.addActionListener(e -> {
            if (ok) System.exit(0);   // TODO: go to My Orders page (Person 5)
            else { new PaymentPage(payment.getAmount()); dispose(); }
        });
        add(btn, BorderLayout.SOUTH);
        setVisible(true);
    }
}
