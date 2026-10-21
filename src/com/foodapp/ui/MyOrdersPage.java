package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.Customer;
import com.foodapp.order.model.Order;
import com.foodapp.order.model.OrderItem;
import com.foodapp.order.model.OrderStatusHistory;
import com.foodapp.order.service.DeliveryTrackingService;
import com.foodapp.order.service.OrderService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MyOrdersPage - Displays customer's orders, order items, and live delivery tracking timeline.
 * Fulfils Person 5's deliverable and connects into HomePage, ExplorePage, and PaymentResultPage.
 */
public class MyOrdersPage extends JFrame {

    private final String customerName;
    private final OrderService orderService = new OrderService();
    private final DeliveryTrackingService trackingService = new DeliveryTrackingService();
    private final JPanel ordersListPanel = new JPanel();

    public MyOrdersPage(String customerName) {
        this.customerName = (customerName != null && !customerName.trim().isEmpty()) ? customerName : "Rahul";

        setTitle("MealFlow - My Orders & Delivery Tracking");
        setSize(960, 680);
        setMinimumSize(new Dimension(800, 540));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ExplorePage.COLOR_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("My Orders & Tracking");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton backBtn = new JButton("← Back to Explore");
        backBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(255, 255, 255, 45));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new ExplorePage(customerName).setVisible(true);
            dispose();
        });
        header.add(backBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Body with ScrollPane
        ordersListPanel.setLayout(new BoxLayout(ordersListPanel, BoxLayout.Y_AXIS));
        ordersListPanel.setBackground(ExplorePage.COLOR_BG);
        ordersListPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        loadOrders();

        JScrollPane scroll = new JScrollPane(ordersListPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadOrders() {
        ordersListPanel.removeAll();

        Customer user = AuthService.getCurrentUser();
        int customerId = (user != null && user.getId() > 0) ? user.getId() : 1;

        List<Order> orders = new ArrayList<>();
        try {
            orders = orderService.getCustomerOrders(customerId);
        } catch (SQLException e) {
            System.err.println("[MyOrdersPage] Could not load orders from MySQL: " + e.getMessage());
        }

        // If no orders returned or DB empty, provide demonstration orders
        if (orders.isEmpty()) {
            Order demo1 = new Order(customerId, 460.0, 0.0, 460.0,
                    "Flat 402, Green Glen Heights, Indiranagar, Bengaluru", "9876543210");
            demo1.setOrderId(1001);
            demo1.setEstimatedDeliveryTime("25-30 mins");
            demo1.setOrderStatus(com.foodapp.order.model.OrderStatus.CONFIRMED);
            demo1.addItem(new OrderItem(11, "Paneer Tikka Brown Rice Bowl", 1, 240.0));
            demo1.addItem(new OrderItem(10, "Smoked Chicken Multigrain Wrap", 1, 220.0));

            Order demo2 = new Order(customerId, 240.0, 0.0, 240.0,
                    "House 12, Koramangala 4th Block, Bengaluru", "9845012345");
            demo2.setOrderId(1002);
            demo2.setEstimatedDeliveryTime("Delivered");
            demo2.setOrderStatus(com.foodapp.order.model.OrderStatus.DELIVERED);
            demo2.addItem(new OrderItem(11, "Paneer Tikka Brown Rice Bowl", 1, 240.0));

            orders.add(demo1);
            orders.add(demo2);
        }

        for (Order order : orders) {
            ordersListPanel.add(createOrderCard(order));
            ordersListPanel.add(Box.createVerticalStrut(16));
        }

        ordersListPanel.revalidate();
        ordersListPanel.repaint();
    }

    private JPanel createOrderCard(Order order) {
        JPanel card = new JPanel(new BorderLayout(16, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ExplorePage.COLOR_SURFACE_HIGH, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        // Top Row: Order ID, Date, Status Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);

        JLabel idLbl = new JLabel("Order #" + order.getOrderId());
        idLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        idLbl.setForeground(ExplorePage.COLOR_TEXT_ON_SURFACE);

        JLabel statusBadge = new JLabel("  " + order.getOrderStatus() + "  ");
        statusBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        statusBadge.setOpaque(true);

        Color badgeBg = new Color(0xDC, 0xF5, 0xE7);
        Color badgeFg = ExplorePage.COLOR_SECONDARY;
        if (order.getOrderStatus() == com.foodapp.order.model.OrderStatus.DELIVERED) {
            badgeBg = new Color(0xE8, 0xF5, 0xE9);
            badgeFg = new Color(0x2E, 0x7D, 0x32);
        } else if (order.getOrderStatus() == com.foodapp.order.model.OrderStatus.CANCELLED) {
            badgeBg = new Color(0xFF, 0xEB, 0xEE);
            badgeFg = Color.RED;
        }
        statusBadge.setBackground(badgeBg);
        statusBadge.setForeground(badgeFg);
        statusBadge.setBorder(BorderFactory.createLineBorder(badgeFg, 1, true));

        topRow.add(idLbl, BorderLayout.WEST);
        topRow.add(statusBadge, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Center: Order Items List & Delivery Address
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        for (OrderItem item : order.getItems()) {
            JPanel itemRow = new JPanel(new BorderLayout());
            itemRow.setBackground(Color.WHITE);
            JLabel name = new JLabel("• " + item.getItemNameSnapshot() + " × " + item.getQuantity());
            name.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            JLabel cost = new JLabel("₹" + (int) item.getSubtotal());
            cost.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            itemRow.add(name, BorderLayout.WEST);
            itemRow.add(cost, BorderLayout.EAST);
            center.add(itemRow);
            center.add(Box.createVerticalStrut(4));
        }

        center.add(Box.createVerticalStrut(6));
        JLabel addr = new JLabel("Delivery Address: " + order.getDeliveryAddress());
        addr.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        addr.setForeground(Color.GRAY);
        center.add(addr);

        card.add(center, BorderLayout.CENTER);

        // Bottom Row: Total & Track Timeline Button
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(Color.WHITE);
        bottomRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ExplorePage.COLOR_SURFACE_HIGH),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)
        ));

        JLabel totalLbl = new JLabel("Grand Total: ₹" + (int) order.getTotalAmount() + " (Delivery: Free)");
        totalLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        totalLbl.setForeground(ExplorePage.COLOR_PRIMARY);

        JButton trackBtn = new JButton("View Status Timeline ⏱");
        trackBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        trackBtn.setBackground(ExplorePage.COLOR_SURFACE_LOW);
        trackBtn.setForeground(ExplorePage.COLOR_TEXT_ON_SURFACE);
        trackBtn.setFocusPainted(false);
        trackBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        trackBtn.addActionListener(e -> showTimelineDialog(order.getOrderId()));

        bottomRow.add(totalLbl, BorderLayout.WEST);
        bottomRow.add(trackBtn, BorderLayout.EAST);
        card.add(bottomRow, BorderLayout.SOUTH);

        return card;
    }

    private void showTimelineDialog(int orderId) {
        List<OrderStatusHistory> timeline = new ArrayList<>();
        try {
            timeline = trackingService.getOrderTimeline(orderId);
        } catch (SQLException ignored) {
        }

        StringBuilder sb = new StringBuilder("<html><body style='width:360px; font-family:sans-serif;'>");
        sb.append("<h2 style='color:#ab2e16;'>Order #").append(orderId).append(" Status Timeline</h2><hr>");

        if (timeline.isEmpty()) {
            sb.append("<p><b>1. PLACED:</b> Order submitted by customer.</p>");
            sb.append("<p><b>2. CONFIRMED:</b> Payment authorized. Kitchen notified.</p>");
            sb.append("<p><b>3. PREPARING:</b> Fresh ingredients assembled by chef.</p>");
        } else {
            for (OrderStatusHistory h : timeline) {
                sb.append("<p><b>").append(h.getNewStatus()).append("</b><br>");
                sb.append("<small style='color:gray;'>").append(h.getNotes()).append("</small><br>");
                if (h.getChangedAt() != null) {
                    sb.append("<small style='color:#006c4a;'>").append(h.getChangedAt()).append("</small>");
                }
                sb.append("</p>");
            }
        }
        sb.append("</body></html>");

        JOptionPane.showMessageDialog(this, new JLabel(sb.toString()), "Delivery Timeline", JOptionPane.INFORMATION_MESSAGE);
    }
}
