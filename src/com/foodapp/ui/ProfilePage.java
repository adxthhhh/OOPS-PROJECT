package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.Customer;

import javax.swing.*;
import java.awt.*;

/**
 * ProfilePage displays the user's profile details including name, email,
 * phone number, customer ID, account status, and subscription information.
 */
public class ProfilePage extends JFrame {

    private final String customerName;

    public ProfilePage(String customerName) {
        this.customerName = (customerName != null && !customerName.trim().isEmpty())
                ? customerName.trim()
                : "Guest User";

        setTitle("User Profile - Food Delivery App");
        setSize(540, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel("User Profile");
        titleLabel.setFont(UIStyle.FONT_HEADING);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton homeBtn = new JButton("< Back to Home");
        homeBtn.setFont(UIStyle.FONT_SMALL);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setBackground(new Color(255, 255, 255, 45));
        homeBtn.setFocusPainted(false);
        homeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        homeBtn.addActionListener(e -> returnToHome());
        headerPanel.add(homeBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Fetch logged-in user or fallback
        Customer user = AuthService.getCurrentUser();
        if (user == null) {
            user = new Customer(
                    this.customerName,
                    this.customerName.toLowerCase().replaceAll("\\s+", "") + "@example.com",
                    "9876543210",
                    ""
            );
            user.setId(101);
        }

        // Center Content with Card
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(UIStyle.BG);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        UIStyle.styleCardPanel(card);
        card.setPreferredSize(new Dimension(460, 520));

        // 1. Large Profile Avatar
        AvatarPanel avatar = new AvatarPanel(76, user.getName());
        avatar.setAlignmentX(CENTER_ALIGNMENT);
        card.add(avatar);
        card.add(Box.createVerticalStrut(12));

        // 2. Name & Status Badge
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font(UIStyle.FONT_HEADING.getName(), Font.BOLD, 22));
        nameLabel.setForeground(UIStyle.TEXT_MAIN);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(6));

        JLabel badge = new JLabel("  VERIFIED CUSTOMER • ACTIVE  ");
        badge.setFont(new Font(UIStyle.FONT_SMALL.getName(), Font.BOLD, 11));
        badge.setForeground(UIStyle.PRIMARY);
        badge.setOpaque(true);
        badge.setBackground(new Color(0xE8F5E9));
        badge.setBorder(BorderFactory.createLineBorder(new Color(0xC8E6C9), 1, true));
        badge.setAlignmentX(CENTER_ALIGNMENT);
        card.add(badge);
        card.add(Box.createVerticalStrut(20));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(420, 1));
        sep.setForeground(new Color(0xE0E0E0));
        card.add(sep);
        card.add(Box.createVerticalStrut(18));

        // 3. User Details Rows
        JPanel detailsPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        detailsPanel.setBackground(UIStyle.CARD_BG);
        detailsPanel.setMaximumSize(new Dimension(420, 180));

        detailsPanel.add(createDetailRow("Customer ID", "#CUST-" + String.format("%04d", user.getId())));
        detailsPanel.add(createDetailRow("Full Name", user.getName()));
        detailsPanel.add(createDetailRow("Email Address", user.getEmail()));
        detailsPanel.add(createDetailRow("Phone Number", user.getPhone()));
        detailsPanel.add(createDetailRow("Subscription Status", "Active (Weekly Plan)"));

        card.add(detailsPanel);
        card.add(Box.createVerticalStrut(24));

        // 4. Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttonPanel.setBackground(UIStyle.CARD_BG);

        JButton backBtn = new JButton("Back to Home");
        UIStyle.styleButton(backBtn, true);
        backBtn.addActionListener(e -> returnToHome());
        buttonPanel.add(backBtn);

        JButton logoutBtn = new JButton("Logout");
        UIStyle.styleButton(logoutBtn, false);
        logoutBtn.addActionListener(e -> doLogout());
        buttonPanel.add(logoutBtn);

        card.add(buttonPanel);

        mainContainer.add(card);
        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(UIStyle.CARD_BG);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIStyle.FONT_BODY);
        lbl.setForeground(UIStyle.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(new Font(UIStyle.FONT_BODY.getName(), Font.BOLD, 14));
        val.setForeground(UIStyle.TEXT_MAIN);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private void returnToHome() {
        new HomePage(customerName).setVisible(true);
        dispose();
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of your account?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            AuthService.logout();
            new LoginPage().setVisible(true);
            dispose();
        }
    }

    /**
     * Circular avatar panel drawing the user's initial.
     */
    private static class AvatarPanel extends JPanel {
        private final int diameter;
        private final String initial;

        public AvatarPanel(int diameter, String name) {
            this.diameter = diameter;
            this.initial = (name != null && !name.trim().isEmpty())
                    ? name.trim().substring(0, 1).toUpperCase()
                    : "U";
            setPreferredSize(new Dimension(diameter, diameter));
            setMaximumSize(new Dimension(diameter, diameter));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Circle background
            g2.setColor(UIStyle.PRIMARY);
            g2.fillOval(0, 0, diameter, diameter);

            // Outer ring
            g2.setColor(new Color(0xC8E6C9));
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(1, 1, diameter - 2, diameter - 2);

            // Initial letter
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(UIStyle.FONT_HEADING.getName(), Font.BOLD, (int) (diameter * 0.48)));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (diameter - fm.stringWidth(initial)) / 2;
            int ty = ((diameter - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(initial, tx, ty);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        UIStyle.setup();
        SwingUtilities.invokeLater(() -> new ProfilePage("Adithya").setVisible(true));
    }
}
