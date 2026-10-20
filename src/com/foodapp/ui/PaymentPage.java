package com.foodapp.ui;

import com.foodapp.payment.Payment;
import com.foodapp.payment.CardPayment;
import com.foodapp.payment.CODPayment;
import com.foodapp.payment.UPIPayment;
import com.foodapp.payment.WalletPayment;

import javax.swing.*;
import java.awt.*;

/** Payment page: choose a method, enter details, click Pay. */
public class PaymentPage extends JFrame {
    private final double amount;
    private final String[] methods = {"UPI", "Card", "Wallet", "Cash on Delivery"};
    private final JRadioButton[] radios = new JRadioButton[4];
    private final CardLayout cards = new CardLayout();
    private final JPanel detailsPanel = new JPanel(cards);

    private final JTextField upiField = new JTextField(20);
    private final JTextField cardNo = new JTextField(20);
    private final JTextField cardName = new JTextField(20);
    private final JTextField cardExp = new JTextField(6);
    private final JPasswordField cardCvv = new JPasswordField(4);
    private final JComboBox<String> walletBox = new JComboBox<>(new String[]{"Paytm", "PhonePe", "Amazon Pay"});
    private final JLabel walletBal = new JLabel("Wallet balance: Rs." + WALLET_BALANCE);
    private final JTextArea codAddress = new JTextArea(3, 20);

    // Demo value; replace with the customer's real wallet balance if you store one.
    private static final double WALLET_BALANCE = 1000.0;

    public PaymentPage(double amount) {
        this.amount = amount;
        setTitle("Payment");
        setSize(460, 440);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel total = new JLabel("Amount to pay: Rs." + amount, SwingConstants.CENTER);
        total.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(total, BorderLayout.NORTH);

        JPanel left = new JPanel(new GridLayout(4, 1));
        left.setBorder(BorderFactory.createTitledBorder("Payment method"));
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            final String m = methods[i];
            radios[i] = new JRadioButton(m, i == 0);
            radios[i].addActionListener(e -> cards.show(detailsPanel, m));
            group.add(radios[i]);
            left.add(radios[i]);
        }
        add(left, BorderLayout.WEST);

        detailsPanel.setBorder(BorderFactory.createTitledBorder("Details"));
        detailsPanel.add(form("UPI ID:", upiField), "UPI");
        detailsPanel.add(form("Card number:", cardNo, "Name on card:", cardName,
                "Expiry (MM/YY):", cardExp, "CVV:", cardCvv), "Card");
        JPanel w = form("Wallet:", walletBox);
        w.add(walletBal);
        detailsPanel.add(w, "Wallet");
        JPanel cod = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cod.add(new JLabel("Delivery address:"));
        cod.add(new JScrollPane(codAddress));
        detailsPanel.add(cod, "Cash on Delivery");
        add(detailsPanel, BorderLayout.CENTER);

        JButton pay = new JButton("Pay Rs." + amount);
        pay.addActionListener(e -> processPayment());
        add(pay, BorderLayout.SOUTH);

        cards.show(detailsPanel, "UPI");
        setVisible(true);
    }

    private JPanel form(Object... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setPreferredSize(new Dimension(250, 200));
        for (Object o : parts) {
            if (o instanceof String) p.add(new JLabel((String) o));
            else p.add((Component) o);
        }
        return p;
    }

    private void processPayment() {
        Payment payment;   // polymorphism: one reference, four possible types
        if (radios[0].isSelected())
            payment = new UPIPayment(amount, upiField.getText());
        else if (radios[1].isSelected())
            payment = new CardPayment(amount, cardNo.getText(), cardName.getText(),
                    cardExp.getText(), new String(cardCvv.getPassword()));
        else if (radios[2].isSelected())
            payment = new WalletPayment(amount, (String) walletBox.getSelectedItem(), WALLET_BALANCE);
        else
            payment = new CODPayment(amount, codAddress.getText());

        payment.pay();
        new PaymentResultPage(payment);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PaymentPage(499.0));
    }
}
