package com.foodapp.ui;

import javax.swing.*;
import java.awt.*;

public class HomePage extends JFrame {

    public HomePage(String customerName) {
        setTitle("Meal Subscription - Home");
        setSize(800, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIStyle.header("Meal Plan & Meal Kit Delivery"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(UIStyle.BG);
        center.setBorder(BorderFactory.createEmptyBorder(50, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome, " + customerName + "!");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 30));
        welcome.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Subscribe once. Eat fresh all week - cooked meals or DIY meal kits.");
        tagline.setFont(UIStyle.BODY);
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        JButton browse = UIStyle.button("Browse Meal Plans");
        browse.setAlignmentX(CENTER_ALIGNMENT);
        browse.addActionListener(e -> {
            new MealPlansPage(customerName).setVisible(true);
            dispose();
        });

        JButton myOrders = UIStyle.button("My Orders");
        myOrders.setAlignmentX(CENTER_ALIGNMENT);
        // TODO (Person 5): open the My Orders page here
        myOrders.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "My Orders page - coming from Person 5"));

        center.add(welcome);
        center.add(Box.createVerticalStrut(12));
        center.add(tagline);
        center.add(Box.createVerticalStrut(40));
        center.add(browse);
        center.add(Box.createVerticalStrut(15));
        center.add(myOrders);

        add(center, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        UIStyle.setup();
        SwingUtilities.invokeLater(() -> new HomePage("Guest").setVisible(true));
    }
}
