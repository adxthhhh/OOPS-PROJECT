package com.foodapp.ui;

import com.foodapp.payment.Payment;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * PaymentResultPage - High-fidelity confirmation receipt screen.
 * Displays successful transaction credentials or detailed failure diagnoses with
 * MealFlow branded visual cards, transaction ID badge, and live tracking navigators.
 */
public class PaymentResultPage extends JFrame {

    public static final Color COLOR_PRIMARY = new Color(0xAB, 0x2E, 0x16);
    public static final Color COLOR_PRIMARY_CONTAINER = new Color(0xCE, 0x46, 0x2C);
    public static final Color COLOR_SECONDARY = new Color(0x00, 0x6C, 0x4A);
    public static final Color COLOR_SECONDARY_CONTAINER = new Color(0x82, 0xF5, 0xC1);
    public static final Color COLOR_SECONDARY_FIXED = new Color(0x85, 0xF8, 0xC4);
    public static final Color COLOR_BACKGROUND = new Color(0xF3, 0xFC, 0xF3);
    public static final Color COLOR_SURFACE_LOWEST = Color.WHITE;
    public static final Color COLOR_SURFACE_LOW = new Color(0xED, 0xF6, 0xEE);
    public static final Color COLOR_SURFACE_HIGH = new Color(0xE2, 0xEA, 0xE2);
    public static final Color COLOR_ON_SURFACE = new Color(0x15, 0x1D, 0x19);
    public static final Color COLOR_ON_SURFACE_VARIANT = new Color(0x59, 0x41, 0x3C);
    public static final Color COLOR_OUTLINE_VARIANT = new Color(0xE1, 0xBF, 0xB8);

    public static final String FONT_NAME = "SansSerif";

    public PaymentResultPage(Payment payment) {
        boolean isSuccess = "SUCCESS".equalsIgnoreCase(payment.getStatus());

        setTitle(isSuccess ? "MealFlow - Order Confirmed!" : "MealFlow - Payment Failed");
        setSize(560, 640);
        setMinimumSize(new Dimension(500, 560));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new GridBagLayout());

        // Receipt Card Shell
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_SURFACE_LOWEST);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 20, 1),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));
        card.setPreferredSize(new Dimension(480, 540));

        // 1. Animated Status Icon (Circular Badge)
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 64;
                int x = (getWidth() - size) / 2;
                int y = 0;

                g2.setColor(isSuccess ? COLOR_SECONDARY_FIXED : new Color(0xFF, 0xDA, 0xD6));
                g2.fillOval(x, y, size, size);

                g2.setColor(isSuccess ? COLOR_SECONDARY : COLOR_PRIMARY);
                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (isSuccess) {
                    // Checkmark
                    g2.drawLine(x + 18, y + 33, x + 28, y + 43);
                    g2.drawLine(x + 28, y + 43, x + 46, y + 22);
                } else {
                    // Cross
                    g2.drawLine(x + 22, y + 22, x + 42, y + 42);
                    g2.drawLine(x + 42, y + 22, x + 22, y + 42);
                }
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(420, 68));
        iconPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        iconPanel.setOpaque(false);
        card.add(iconPanel);
        card.add(Box.createVerticalStrut(10));

        // 2. Heading Title & Subtitle
        JLabel titleLbl = new JLabel(isSuccess ? "Payment Successful!" : "Payment Unsuccessful", SwingConstants.CENTER);
        titleLbl.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        titleLbl.setForeground(isSuccess ? COLOR_SECONDARY : COLOR_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));

        JLabel subLbl = new JLabel(isSuccess ? "Your order has been received and sent to the kitchen." : "We could not process your transaction.", SwingConstants.CENTER);
        subLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        subLbl.setForeground(COLOR_ON_SURFACE_VARIANT);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subLbl);
        card.add(Box.createVerticalStrut(18));

        // 3. Receipt Details Box
        JPanel receipt = new JPanel();
        receipt.setLayout(new BoxLayout(receipt, BoxLayout.Y_AXIS));
        receipt.setBackground(COLOR_SURFACE_LOW);
        receipt.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 12, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        receipt.add(createReceiptRow("Amount Paid", String.format("₹%.2f", payment.getAmount()), true));
        receipt.add(Box.createVerticalStrut(8));
        receipt.add(createReceiptRow("Payment Method", payment.getMethodName(), false));
        receipt.add(Box.createVerticalStrut(8));

        if (payment.getTransactionId() != null && !payment.getTransactionId().isEmpty()) {
            receipt.add(createReceiptRow("Transaction ID", payment.getTransactionId(), false));
            receipt.add(Box.createVerticalStrut(8));
        }

        receipt.add(createReceiptRow("Status", payment.getStatus(), false));

        if (payment.getMessage() != null && !payment.getMessage().isEmpty()) {
            receipt.add(Box.createVerticalStrut(8));
            JLabel msgLbl = new JLabel("<html><body style='width:340px;'><font color='#59413c' size='-1'>" + payment.getMessage() + "</font></body></html>");
            receipt.add(msgLbl);
        }

        card.add(receipt);
        card.add(Box.createVerticalStrut(16));

        // 4. Live Delivery Pill if Successful
        if (isSuccess) {
            JPanel dispatchPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            dispatchPill.setBackground(COLOR_SECONDARY_CONTAINER);
            dispatchPill.setBorder(new RoundedBorder(COLOR_SECONDARY, 8, 1));
            JLabel bolt = new JLabel("⚡ Dispatching in 30-35 mins to your saved address");
            bolt.setFont(new Font(FONT_NAME, Font.BOLD, 11));
            bolt.setForeground(COLOR_SECONDARY);
            dispatchPill.add(bolt);
            dispatchPill.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(dispatchPill);
            card.add(Box.createVerticalStrut(18));
        }

        // 5. Action CTA Buttons
        JButton primaryBtn = new JButton(isSuccess ? "🧾 View My Orders & Live Tracking" : "🔄 Try Again");
        primaryBtn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        primaryBtn.setBackground(isSuccess ? COLOR_SECONDARY : COLOR_PRIMARY);
        primaryBtn.setForeground(Color.WHITE);
        primaryBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        primaryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        primaryBtn.setFocusPainted(false);
        primaryBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        primaryBtn.setBorder(new RoundedBorder(isSuccess ? COLOR_SECONDARY : COLOR_PRIMARY_CONTAINER, 10, 1));

        primaryBtn.addActionListener(e -> {
            if (isSuccess) {
                new MyOrdersPage("Customer").setVisible(true);
                dispose();
            } else {
                new PaymentPage(payment.getAmount()).setVisible(true);
                dispose();
            }
        });
        card.add(primaryBtn);
        card.add(Box.createVerticalStrut(8));

        JButton backBtn = new JButton("🧭 Back to Explore");
        backBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        backBtn.setForeground(COLOR_ON_SURFACE_VARIANT);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new ExplorePage("Customer").setVisible(true);
            dispose();
        });
        card.add(backBtn);

        add(card);
        setVisible(true);
    }

    private JPanel createReceiptRow(String title, String val, boolean isBoldLarge) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        tLbl.setForeground(COLOR_ON_SURFACE_VARIANT);

        JLabel vLbl = new JLabel(val);
        vLbl.setFont(new Font(FONT_NAME, isBoldLarge ? Font.BOLD : Font.PLAIN, isBoldLarge ? 16 : 12));
        vLbl.setForeground(isBoldLarge ? COLOR_PRIMARY : COLOR_ON_SURFACE);

        row.add(tLbl, BorderLayout.WEST);
        row.add(vLbl, BorderLayout.EAST);
        return row;
    }

    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 1, thickness + 1, thickness + 1, thickness + 1);
        }
    }
}
