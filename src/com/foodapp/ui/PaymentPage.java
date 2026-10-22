package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.Customer;
import com.foodapp.order.model.Order;
import com.foodapp.order.model.OrderItem;
import com.foodapp.order.service.OrderService;
import com.foodapp.payment.*;
import com.foodapp.payment.service.PaymentService;
import com.foodapp.util.ImageLoader;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PaymentPage - Modern On-Demand Checkout & Payment UI.
 * Full Java Swing equivalent of the high-fidelity Tailwind Checkout HTML/CSS.
 *
 * Features:
 * - Persistent left navigation sidebar with active plan widget
 * - Modern top bar with address picker, instant search with ⌘K, notifications, and profile
 * - Consolidated vendor banner ("The Green Kitchen")
 * - Dynamic cart dish cards with quantity steppers, addon pills, and live bill recalculation
 * - Multiple delivery addresses (Home / Work / + Add New)
 * - Rider instructions pills (Leave with security, Avoid calling, Ring doorbell twice, Leave at door)
 * - MealFlow Cash redemption toggle (₹460 balance, Apply ₹200)
 * - Indian context payment options: UPI (Google Pay, PhonePe, Paytm, BHIM, CRED with VPA verification),
 *   Credit/Debit Cards (Saved cards + new card details dialog), and Cash on Delivery
 * - Promo code panel with live 'SAVEMF' ₹50 discount apply/remove
 * - Calorie / Macro sparkline gauge (840 kcal, Protein 38g, Carbs 54g, Fats 18g)
 * - Live itemized bill breakdown (subtotal, packaging, delivery fee, GST, discounts, grand total)
 * - Polymorphic OOP Payment execution via UPIPayment, CardPayment, WalletPayment, CODPayment
 * - Automatic Order & OrderItem persistence into MySQL/in-memory fallback, routing to PaymentResultPage
 */
public class PaymentPage extends JFrame {

    // --- Color Palette (Tailwind Design System Tokens) ---
    public static final Color COLOR_PRIMARY = new Color(0xAB, 0x2E, 0x16);
    public static final Color COLOR_PRIMARY_CONTAINER = new Color(0xCE, 0x46, 0x2C);
    public static final Color COLOR_PRIMARY_FIXED = new Color(0xFF, 0xDA, 0xD3);
    public static final Color COLOR_ON_PRIMARY_FIXED = new Color(0x3E, 0x04, 0x00);
    public static final Color COLOR_SECONDARY = new Color(0x00, 0x6C, 0x4A);
    public static final Color COLOR_SECONDARY_CONTAINER = new Color(0x82, 0xF5, 0xC1);
    public static final Color COLOR_SECONDARY_FIXED = new Color(0x85, 0xF8, 0xC4);
    public static final Color COLOR_ON_SECONDARY_FIXED = new Color(0x00, 0x21, 0x14);
    public static final Color COLOR_ON_SECONDARY_CONTAINER = new Color(0x00, 0x71, 0x4E);
    public static final Color COLOR_TERTIARY = new Color(0xA3, 0x39, 0x00);
    public static final Color COLOR_BACKGROUND = new Color(0xF3, 0xFC, 0xF3);
    public static final Color COLOR_SURFACE_LOWEST = Color.WHITE;
    public static final Color COLOR_SURFACE_LOW = new Color(0xED, 0xF6, 0xEE);
    public static final Color COLOR_SURFACE_CONTAINER = new Color(0xE7, 0xF0, 0xE8);
    public static final Color COLOR_SURFACE_HIGH = new Color(0xE2, 0xEA, 0xE2);
    public static final Color COLOR_SURFACE_HIGHEST = new Color(0xDC, 0xE5, 0xDD);
    public static final Color COLOR_ON_SURFACE = new Color(0x15, 0x1D, 0x19);
    public static final Color COLOR_ON_SURFACE_VARIANT = new Color(0x59, 0x41, 0x3C);
    public static final Color COLOR_OUTLINE = new Color(0x8D, 0x70, 0x6B);
    public static final Color COLOR_OUTLINE_VARIANT = new Color(0xE1, 0xBF, 0xB8);
    public static final Color COLOR_INVERSE_SURFACE = new Color(0x2A, 0x32, 0x2D);

    public static final String FONT_NAME = "SansSerif";

    // --- State & Models ---
    private final String customerName;
    private final OrderService orderService = new OrderService();
    private final PaymentService paymentService = new PaymentService();

    // Cart items state
    private final List<CheckoutCartItem> cartItems = new ArrayList<>();

    // Delivery Address
    private final JRadioButton homeAddressRadio = new JRadioButton();
    private final JRadioButton workAddressRadio = new JRadioButton();
    private String selectedAddressText = "Flat 402, Green Glen Layout, Bellandur, Bengaluru, Karnataka 560103";
    private String customerPhone = "9876543210";

    // Rider instructions
    private String activeRiderPreference = "Avoid calling";
    private final JTextField riderNotesField = new JTextField();

    // Payment methods
    private final JRadioButton upiRadio = new JRadioButton();
    private final JRadioButton cardRadio = new JRadioButton();
    private final JRadioButton codRadio = new JRadioButton();
    private final JTextField upiIdField = new JTextField("rahul.sharma@okaxis");

    // Card fields
    private String cardLast4 = "4082";
    private String cardExpiry = "09/27";
    private String cardHolder = "Rahul Sharma";

    // Cash & Coupon
    private final JCheckBox mealFlowCashCheck = new JCheckBox("Apply ₹200");
    private boolean isCouponApplied = true;
    private final JTextField couponInputField = new JTextField("SAVEMF");
    private JPanel couponActiveBadge;

    // Live Bill labels
    private JLabel subtotalLabel;
    private JLabel packagingLabel;
    private JLabel deliveryFeeLabel;
    private JLabel gstLabel;
    private JLabel discountLabel;
    private JLabel cashDiscountLabel;
    private JLabel grandTotalLabel;
    private JLabel savingsLabel;
    private JButton placeOrderBtn;
    private JLabel cartCountHeader;

    // Toast overlay
    private JPanel toastPanel;
    private JLabel toastLabel;
    private Timer toastTimer;

    // Constructors
    public PaymentPage() {
        this("Rahul", 482.50);
    }

    public PaymentPage(double initialAmount) {
        this("Rahul", initialAmount);
    }

    public PaymentPage(String customerName) {
        this(customerName, 482.50);
    }

    public PaymentPage(String customerName, double amount) {
        Customer current = AuthService.getCurrentUser();
        this.customerName = (current != null && current.getName() != null && !current.getName().trim().isEmpty())
                ? current.getName()
                : ((customerName != null && !customerName.trim().isEmpty()) ? customerName : "Rahul");

        if (current != null && current.getPhone() != null && !current.getPhone().isEmpty()) {
            this.customerPhone = current.getPhone();
        }

        // Initialize standard default dishes in cart matching HTML
        initDefaultCartItems();

        setTitle("MealFlow - On-Demand Checkout & Review");
        setSize(1340, 880);
        setMinimumSize(new Dimension(1080, 720));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Left Navigation Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Center Content Area (Header + Scrollable Checkout Stage)
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(COLOR_BACKGROUND);

        mainWrapper.add(createHeader(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(createCheckoutContent());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setBackground(COLOR_BACKGROUND);
        scrollPane.getViewport().setBackground(COLOR_BACKGROUND);
        mainWrapper.add(scrollPane, BorderLayout.CENTER);

        add(mainWrapper, BorderLayout.CENTER);

        // Floating Toast
        setupFloatingToast();

        // Initial Calculation
        recalculateBill();
    }

    private void initDefaultCartItems() {
        cartItems.clear();
        cartItems.add(new CheckoutCartItem(
                11,
                "Paneer Tikka Brown Rice Bowl",
                "Quinoa Pilaf (+₹40) • Extra Mint Chutney (+₹15)",
                "Base: ₹240 • Prep: 15 mins",
                295.0,
                1,
                "cart_paneer_tikka.jpg"
        ));
        cartItems.add(new CheckoutCartItem(
                12,
                "Cold-Pressed Valencia Orange Juice (300ml)",
                "No added sugar • 100% Raw",
                "₹110 each",
                110.0,
                2,
                "cart_orange_juice.jpg"
        ));
        cartItems.add(new CheckoutCartItem(
                13,
                "Roasted Makhana Snack Pot (45g)",
                "Himalayan Pink Salt & Pepper",
                "₹60 each",
                60.0,
                1,
                "cart_makhana.jpg"
        ));
    }

    // =========================================================================
    // 1. FIXED LEFT SIDEBAR
    // =========================================================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 800));
        sidebar.setBackground(COLOR_SURFACE_LOWEST);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_OUTLINE_VARIANT));

        // Top: Logo + Nav Items
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        // Brand Logo
        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);

        JLabel logoIcon = new JLabel("🍃");
        logoIcon.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        logoIcon.setForeground(COLOR_SECONDARY);

        JLabel brandName = new JLabel("MealFlow");
        brandName.setFont(new Font(FONT_NAME, Font.BOLD, 20));
        brandName.setForeground(COLOR_ON_SURFACE);

        brandRow.add(logoIcon);
        brandRow.add(brandName);
        topSection.add(brandRow);
        topSection.add(Box.createVerticalStrut(24));

        // Navigation Items
        topSection.add(createNavItem("🧭 Explore", false, () -> {
            new ExplorePage(customerName).setVisible(true);
            dispose();
        }));
        topSection.add(createNavItem("🍽 Meal Plans", false, () -> {
            new MealPlansPage(customerName).setVisible(true);
            dispose();
        }));
        topSection.add(createNavItem("🧾 My Orders", false, () -> {
            new MyOrdersPage(customerName).setVisible(true);
            dispose();
        }));
        topSection.add(createNavItem("🔄 My Subscriptions", false, () -> showSubscriptionsDialog()));
        topSection.add(createNavItem("⚙ Profile & Preferences", false, () -> {
            new ProfilePage(customerName).setVisible(true);
            dispose();
        }));

        sidebar.add(topSection, BorderLayout.NORTH);

        // Bottom: Active Plan + Support + MVP Badge
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setOpaque(false);
        bottomSection.setBorder(BorderFactory.createEmptyBorder(16, 16, 20, 16));

        // Active Plan Widget
        JPanel activePlanCard = new JPanel(new BorderLayout(6, 4));
        activePlanCard.setBackground(COLOR_SURFACE_LOW);
        activePlanCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JPanel apTop = new JPanel(new BorderLayout());
        apTop.setOpaque(false);
        JLabel apTag = new JLabel("ACTIVE PLAN");
        apTag.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        apTag.setForeground(COLOR_SECONDARY);
        JLabel dot = new JLabel("●");
        dot.setForeground(COLOR_SECONDARY);
        apTop.add(apTag, BorderLayout.WEST);
        apTop.add(dot, BorderLayout.EAST);

        JLabel apName = new JLabel("Balanced Week");
        apName.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        apName.setForeground(COLOR_ON_SURFACE);

        activePlanCard.add(apTop, BorderLayout.NORTH);
        activePlanCard.add(apName, BorderLayout.CENTER);
        bottomSection.add(activePlanCard);
        bottomSection.add(Box.createVerticalStrut(12));

        JButton supportBtn = new JButton("💬 Support & Help");
        supportBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        supportBtn.setForeground(COLOR_ON_SURFACE_VARIANT);
        supportBtn.setContentAreaFilled(false);
        supportBtn.setBorderPainted(false);
        supportBtn.setFocusPainted(false);
        supportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        supportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "MealFlow Concierge Support\nAvailable 24/7 at support@mealflow.in or +91 80 4000 9999",
                "Customer Care", JOptionPane.INFORMATION_MESSAGE));
        bottomSection.add(supportBtn);
        bottomSection.add(Box.createVerticalStrut(8));

        JLabel versionLbl = new JLabel("v1.0 MVP • Desktop App");
        versionLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        versionLbl.setForeground(COLOR_OUTLINE);
        versionLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomSection.add(versionLbl);

        sidebar.add(bottomSection, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavItem(String text, boolean active, Runnable onClick) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, active ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(active ? Color.WHITE : COLOR_ON_SURFACE_VARIANT);
        btn.setBackground(active ? COLOR_PRIMARY : COLOR_SURFACE_LOWEST);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    btn.setBackground(COLOR_SURFACE_CONTAINER);
                    btn.setForeground(COLOR_ON_SURFACE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) {
                    btn.setBackground(COLOR_SURFACE_LOWEST);
                    btn.setForeground(COLOR_ON_SURFACE_VARIANT);
                }
            }
        });

        if (onClick != null) {
            btn.addActionListener(e -> onClick.run());
        }
        return btn;
    }

    // =========================================================================
    // 2. HEADER BAR
    // =========================================================================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setPreferredSize(new Dimension(1000, 64));
        header.setBackground(COLOR_SURFACE_LOWEST);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_OUTLINE_VARIANT),
                BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));

        // Location selector button
        JButton locBtn = new JButton("📍 Indiranagar 100ft Rd, Bengaluru ▾");
        locBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        locBtn.setForeground(COLOR_ON_SURFACE);
        locBtn.setContentAreaFilled(false);
        locBtn.setBorderPainted(false);
        locBtn.setFocusPainted(false);
        locBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        locBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Current Delivery Location:\nIndiranagar 100ft Road, Bengaluru, 560038\n\nDelivery Radius: 4.5 km",
                "Delivery Address", JOptionPane.INFORMATION_MESSAGE));
        header.add(locBtn, BorderLayout.WEST);

        // Center Search Bar with ⌘K badge
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setBackground(COLOR_SURFACE_LOW);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        searchBox.setPreferredSize(new Dimension(380, 36));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setForeground(COLOR_OUTLINE);
        JTextField searchField = new JTextField("Search dishes, cuisines, meal plans, or vendors...");
        searchField.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        searchField.setForeground(COLOR_OUTLINE);
        searchField.setBorder(null);
        searchField.setOpaque(false);

        JLabel cmdK = new JLabel(" ⌘K ");
        cmdK.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        cmdK.setOpaque(true);
        cmdK.setBackground(COLOR_SURFACE_HIGH);
        cmdK.setForeground(COLOR_ON_SURFACE_VARIANT);
        cmdK.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);
        searchBox.add(cmdK, BorderLayout.EAST);
        header.add(searchBox, BorderLayout.CENTER);

        // Right Actions (Notifications, Cart, Profile)
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton notifBtn = new JButton("🔔");
        notifBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        notifBtn.setContentAreaFilled(false);
        notifBtn.setBorderPainted(false);
        notifBtn.setFocusPainted(false);
        notifBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        cartCountHeader = new JLabel("🛍 Cart (3) • ₹460");
        cartCountHeader.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        cartCountHeader.setOpaque(true);
        cartCountHeader.setBackground(COLOR_PRIMARY_FIXED);
        cartCountHeader.setForeground(COLOR_ON_PRIMARY_FIXED);
        cartCountHeader.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JButton profileBtn = new JButton("👤 " + customerName + " ▾");
        profileBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        profileBtn.setContentAreaFilled(false);
        profileBtn.setBorderPainted(false);
        profileBtn.setFocusPainted(false);
        profileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profileBtn.addActionListener(e -> {
            new ProfilePage(customerName).setVisible(true);
            dispose();
        });

        rightActions.add(notifBtn);
        rightActions.add(cartCountHeader);
        rightActions.add(profileBtn);
        header.add(rightActions, BorderLayout.EAST);

        return header;
    }

    // =========================================================================
    // 3. CHECKOUT CONTENT (2-COLUMN RESPONSIVE LAYOUT)
    // =========================================================================
    private JPanel createCheckoutContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 36, 32));

        // Breadcrumb + Header Title
        content.add(createCheckoutHeader());
        content.add(Box.createVerticalStrut(20));

        // Two-column split stage
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(COLOR_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;

        // Left Column (Cart, Address, Slot, Payment Methods) - 65% width
        gbc.gridx = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 20);
        grid.add(createLeftCheckoutColumn(), gbc);

        // Right Column (Promo, Bill Details, Macro gauge, Primary CTA) - 35% width
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        grid.add(createRightCheckoutColumn(), gbc);

        content.add(grid);
        return content;
    }

    private JPanel createCheckoutHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftTitle = new JPanel();
        leftTitle.setLayout(new BoxLayout(leftTitle, BoxLayout.Y_AXIS));
        leftTitle.setOpaque(false);

        // Breadcrumb
        JLabel breadcrumb = new JLabel("Explore > Cart > Review & Delivery Details");
        breadcrumb.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        breadcrumb.setForeground(COLOR_OUTLINE);
        leftTitle.add(breadcrumb);
        leftTitle.add(Box.createVerticalStrut(4));

        // Title Row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        JLabel h1 = new JLabel("On-Demand Checkout");
        h1.setFont(new Font(FONT_NAME, Font.BOLD, 26));
        h1.setForeground(COLOR_ON_SURFACE);

        JLabel badge = new JLabel(" A La Carte Order ");
        badge.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(COLOR_SECONDARY_CONTAINER);
        badge.setForeground(COLOR_ON_SECONDARY_CONTAINER);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        titleRow.add(h1);
        titleRow.add(badge);
        leftTitle.add(titleRow);

        headerPanel.add(leftTitle, BorderLayout.WEST);

        // Express Dispatch Pill
        JPanel dispatchPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        dispatchPill.setBackground(COLOR_SURFACE_LOW);
        dispatchPill.setBorder(new RoundedBorder(COLOR_OUTLINE_VARIANT, 12, 1));
        JLabel bolt = new JLabel("⚡ Express Dispatch: ");
        bolt.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        bolt.setForeground(COLOR_ON_SURFACE);
        JLabel time = new JLabel("30-35 mins");
        time.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        time.setForeground(COLOR_SECONDARY);
        dispatchPill.add(bolt);
        dispatchPill.add(time);

        headerPanel.add(dispatchPill, BorderLayout.EAST);
        return headerPanel;
    }

    // -------------------------------------------------------------------------
    // LEFT CHECKOUT COLUMN
    // -------------------------------------------------------------------------
    private JPanel createLeftCheckoutColumn() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // 1. Vendor Banner
        left.add(createVendorBanner());
        left.add(Box.createVerticalStrut(18));

        // 2. Dishes in Cart
        left.add(createCartDishesCard());
        left.add(Box.createVerticalStrut(18));

        // 3. Delivery Address Selection
        left.add(createDeliveryAddressCard());
        left.add(Box.createVerticalStrut(18));

        // 4. Delivery Slot & Instructions
        left.add(createDeliverySlotCard());
        left.add(Box.createVerticalStrut(18));

        // 5. Payment Method Selection (Indian Context)
        left.add(createPaymentMethodCard());

        return left;
    }

    private JPanel createVendorBanner() {
        JPanel banner = new JPanel(new BorderLayout(14, 0));
        banner.setBackground(COLOR_SURFACE_LOWEST);
        banner.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 16, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        // Storefront icon + text
        JPanel vendorInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        vendorInfo.setOpaque(false);

        JLabel storeIcon = new JLabel("🏪");
        storeIcon.setFont(new Font(FONT_NAME, Font.PLAIN, 32));
        storeIcon.setOpaque(true);
        storeIcon.setBackground(COLOR_SURFACE_LOW);
        storeIcon.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        JPanel titleBadgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleBadgeRow.setOpaque(false);
        JLabel vendorTitle = new JLabel("The Green Kitchen");
        vendorTitle.setFont(new Font(FONT_NAME, Font.BOLD, 17));
        vendorTitle.setForeground(COLOR_ON_SURFACE);
        JLabel area = new JLabel(" Indiranagar ");
        area.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        area.setOpaque(true);
        area.setBackground(COLOR_SURFACE_HIGH);
        area.setForeground(COLOR_ON_SURFACE_VARIANT);
        area.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        titleBadgeRow.add(vendorTitle);
        titleBadgeRow.add(area);

        JLabel subNote = new JLabel("✓ All items prepared fresh in one kitchen for consolidated delivery");
        subNote.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        subNote.setForeground(COLOR_ON_SURFACE_VARIANT);

        textStack.add(titleBadgeRow);
        textStack.add(Box.createVerticalStrut(3));
        textStack.add(subNote);

        vendorInfo.add(storeIcon);
        vendorInfo.add(textStack);
        banner.add(vendorInfo, BorderLayout.CENTER);

        JButton addMore = new JButton("+ Add more items →");
        addMore.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        addMore.setForeground(COLOR_PRIMARY);
        addMore.setContentAreaFilled(false);
        addMore.setBorderPainted(false);
        addMore.setFocusPainted(false);
        addMore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMore.addActionListener(e -> {
            new ExplorePage(customerName).setVisible(true);
            dispose();
        });
        banner.add(addMore, BorderLayout.EAST);

        return banner;
    }

    private JPanel createCartDishesCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_SURFACE_LOWEST);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 16, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel h = new JLabel("🧺 Dishes in Cart (" + cartItems.size() + ")");
        h.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        h.setForeground(COLOR_ON_SURFACE);

        JLabel pkgNote = new JLabel("Consolidated Packaging");
        pkgNote.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        pkgNote.setForeground(COLOR_OUTLINE);

        cardHeader.add(h, BorderLayout.WEST);
        cardHeader.add(pkgNote, BorderLayout.EAST);
        card.add(cardHeader);
        card.add(Box.createVerticalStrut(14));

        // List each dish item row
        for (CheckoutCartItem item : cartItems) {
            card.add(createDishRow(item));
            card.add(Box.createVerticalStrut(10));
        }

        return card;
    }

    private JPanel createDishRow(CheckoutCartItem item) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setBackground(COLOR_SURFACE_LOW);
        row.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 10, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Thumbnail on WEST
        if (item.imageKey != null && !item.imageKey.isEmpty()) {
            JPanel thumbPanel = ImageLoader.createImagePanel(item.imageKey, 56, 56, 8);
            thumbPanel.setPreferredSize(new Dimension(56, 56));
            row.add(thumbPanel, BorderLayout.WEST);
        }

        // Dish details on center
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JPanel titleVegRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleVegRow.setOpaque(false);
        JLabel vegDot = new JLabel("●");
        vegDot.setForeground(COLOR_SECONDARY);
        JLabel nameLbl = new JLabel(item.name);
        nameLbl.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        nameLbl.setForeground(COLOR_ON_SURFACE);
        titleVegRow.add(vegDot);
        titleVegRow.add(nameLbl);

        JLabel addonLbl = new JLabel(item.addons);
        addonLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        addonLbl.setForeground(COLOR_ON_SURFACE_VARIANT);

        JLabel metaLbl = new JLabel(item.prepNotes);
        metaLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        metaLbl.setForeground(COLOR_OUTLINE);

        infoPanel.add(titleVegRow);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(addonLbl);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(metaLbl);
        row.add(infoPanel, BorderLayout.CENTER);

        // Right side: Stepper + Price + Delete
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        // Stepper
        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        stepper.setBackground(COLOR_SURFACE_LOWEST);
        stepper.setBorder(new RoundedBorder(COLOR_SURFACE_HIGH, 6, 1));

        JButton minusBtn = new JButton("−");
        minusBtn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        minusBtn.setContentAreaFilled(false);
        minusBtn.setBorderPainted(false);
        minusBtn.setFocusPainted(false);
        minusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel qtyLbl = new JLabel(String.valueOf(item.quantity), SwingConstants.CENTER);
        qtyLbl.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        qtyLbl.setPreferredSize(new Dimension(20, 20));

        JButton plusBtn = new JButton("+");
        plusBtn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        plusBtn.setContentAreaFilled(false);
        plusBtn.setBorderPainted(false);
        plusBtn.setFocusPainted(false);
        plusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lineTotalLbl = new JLabel(String.format("₹%.0f", item.unitPrice * item.quantity));
        lineTotalLbl.setFont(new Font(FONT_NAME, Font.BOLD, 15));
        lineTotalLbl.setForeground(COLOR_ON_SURFACE);
        lineTotalLbl.setPreferredSize(new Dimension(65, 20));
        lineTotalLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        minusBtn.addActionListener(e -> {
            if (item.quantity > 1) {
                item.quantity--;
                qtyLbl.setText(String.valueOf(item.quantity));
                lineTotalLbl.setText(String.format("₹%.0f", item.unitPrice * item.quantity));
                recalculateBill();
            }
        });

        plusBtn.addActionListener(e -> {
            item.quantity++;
            qtyLbl.setText(String.valueOf(item.quantity));
            lineTotalLbl.setText(String.format("₹%.0f", item.unitPrice * item.quantity));
            recalculateBill();
        });

        stepper.add(minusBtn);
        stepper.add(qtyLbl);
        stepper.add(plusBtn);

        JButton delBtn = new JButton("🗑");
        delBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        delBtn.setContentAreaFilled(false);
        delBtn.setBorderPainted(false);
        delBtn.setFocusPainted(false);
        delBtn.setForeground(COLOR_OUTLINE);
        delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> {
            showToast("Item cannot be removed in demo cart.");
        });

        rightPanel.add(stepper);
        rightPanel.add(lineTotalLbl);
        rightPanel.add(delBtn);

        row.add(rightPanel, BorderLayout.EAST);
        return row;
    }

    private JPanel createDeliveryAddressCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_SURFACE_LOWEST);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 16, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("📍 Delivery Address");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        title.setForeground(COLOR_ON_SURFACE);

        JButton addBtn = new JButton("+ Add New Address");
        addBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        addBtn.setForeground(COLOR_PRIMARY);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> {
            String newAddr = JOptionPane.showInputDialog(this, "Enter New Delivery Address:", "Add Address", JOptionPane.PLAIN_MESSAGE);
            if (newAddr != null && !newAddr.trim().isEmpty()) {
                selectedAddressText = newAddr.trim();
                showToast("Delivery address updated!");
            }
        });

        cardHeader.add(title, BorderLayout.WEST);
        cardHeader.add(addBtn, BorderLayout.EAST);
        card.add(cardHeader);
        card.add(Box.createVerticalStrut(14));

        // Address 1: Home (Default)
        ButtonGroup addrGroup = new ButtonGroup();
        homeAddressRadio.setSelected(true);
        addrGroup.add(homeAddressRadio);
        addrGroup.add(workAddressRadio);

        JPanel homeCard = createAddressTile(
                homeAddressRadio,
                "Home",
                "Default",
                "Flat 402, Green Glen Layout, Bellandur, Bengaluru, Karnataka 560103",
                "📞 +91 98765 43210",
                true
        );

        JPanel workCard = createAddressTile(
                workAddressRadio,
                "Work",
                null,
                "Tower B, 3rd Floor, RMZ Infinity, Old Madras Rd, Indiranagar, Bengaluru 560038",
                "🏢 Tech Reception",
                false
        );

        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setOpaque(false);
        grid.add(homeCard);
        grid.add(workCard);
        card.add(grid);

        return card;
    }

    private JPanel createAddressTile(JRadioButton radio, String title, String tag, String address, String note, boolean selected) {
        JPanel tile = new JPanel(new BorderLayout(8, 6));
        tile.setBackground(selected ? COLOR_SURFACE_LOW : COLOR_SURFACE_LOWEST);
        tile.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(selected ? COLOR_PRIMARY : COLOR_OUTLINE_VARIANT, 12, selected ? 2 : 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        radio.setOpaque(false);
        radio.addActionListener(e -> {
            selectedAddressText = address;
            showToast("Selected " + title + " address.");
        });

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        tLbl.setForeground(COLOR_ON_SURFACE);
        top.add(tLbl);

        if (tag != null) {
            JLabel tagLbl = new JLabel(" " + tag + " ");
            tagLbl.setFont(new Font(FONT_NAME, Font.BOLD, 10));
            tagLbl.setOpaque(true);
            tagLbl.setBackground(COLOR_PRIMARY_FIXED);
            tagLbl.setForeground(COLOR_ON_PRIMARY_FIXED);
            tagLbl.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
            top.add(tagLbl);
        }

        JLabel addrLbl = new JLabel("<html><body style='width:180px;'>" + address + "</body></html>");
        addrLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        addrLbl.setForeground(COLOR_ON_SURFACE_VARIANT);

        JLabel noteLbl = new JLabel(note);
        noteLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        noteLbl.setForeground(COLOR_OUTLINE);

        info.add(top);
        info.add(Box.createVerticalStrut(4));
        info.add(addrLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(noteLbl);

        tile.add(radio, BorderLayout.WEST);
        tile.add(info, BorderLayout.CENTER);

        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio.setSelected(true);
                selectedAddressText = address;
                showToast("Selected " + title + " address.");
            }
        });

        return tile;
    }

    private JPanel createDeliverySlotCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_SURFACE_LOWEST);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 16, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("⏱ Delivery Slot & Instructions");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        title.setForeground(COLOR_ON_SURFACE);

        JLabel rushPill = new JLabel(" ● Arriving in 30-35 mins (Standard Rush) ");
        rushPill.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        rushPill.setOpaque(true);
        rushPill.setBackground(COLOR_SECONDARY_CONTAINER);
        rushPill.setForeground(COLOR_ON_SECONDARY_CONTAINER);
        rushPill.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        cardHeader.add(title, BorderLayout.WEST);
        cardHeader.add(rushPill, BorderLayout.EAST);
        card.add(cardHeader);
        card.add(Box.createVerticalStrut(12));

        // Rider preferences pills
        JLabel riderTag = new JLabel("INSTANT RIDER PREFERENCES");
        riderTag.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        riderTag.setForeground(COLOR_ON_SURFACE_VARIANT);
        card.add(riderTag);
        card.add(Box.createVerticalStrut(8));

        JPanel pillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pillsPanel.setOpaque(false);

        String[] options = {"Leave with security", "Avoid calling", "Ring doorbell twice", "Leave at door"};
        for (String opt : options) {
            pillsPanel.add(createRiderPreferencePill(opt));
        }
        card.add(pillsPanel);
        card.add(Box.createVerticalStrut(10));

        // Note for delivery executive
        JLabel notesLabel = new JLabel("Add note for the delivery executive");
        notesLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        notesLabel.setForeground(COLOR_OUTLINE);
        card.add(notesLabel);
        card.add(Box.createVerticalStrut(4));

        riderNotesField.setText("e.g. Please do not blow horn at the gate, take elevator to 4th floor");
        riderNotesField.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        riderNotesField.setForeground(COLOR_ON_SURFACE);
        riderNotesField.setBackground(COLOR_SURFACE_LOW);
        riderNotesField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.add(riderNotesField);

        return card;
    }

    private JButton createRiderPreferencePill(String text) {
        boolean isSelected = text.equals(activeRiderPreference);
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        btn.setBackground(isSelected ? COLOR_PRIMARY : COLOR_SURFACE_CONTAINER);
        btn.setForeground(isSelected ? Color.WHITE : COLOR_ON_SURFACE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(isSelected ? COLOR_PRIMARY_CONTAINER : COLOR_SURFACE_HIGH, 14, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        btn.addActionListener(e -> {
            activeRiderPreference = text;
            showToast("Rider instruction: " + text);
        });
        return btn;
    }

    private JPanel createPaymentMethodCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_SURFACE_LOWEST);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 16, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("💳 Payment Method");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        title.setForeground(COLOR_ON_SURFACE);

        JLabel sslBadge = new JLabel(" 🔒 256-Bit SSL Encrypted ");
        sslBadge.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        sslBadge.setForeground(COLOR_SECONDARY);

        cardHeader.add(title, BorderLayout.WEST);
        cardHeader.add(sslBadge, BorderLayout.EAST);
        card.add(cardHeader);
        card.add(Box.createVerticalStrut(14));

        // MealFlow Cash Balance Widget
        JPanel cashWidget = new JPanel(new BorderLayout(12, 0));
        cashWidget.setBackground(COLOR_SURFACE_CONTAINER);
        cashWidget.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 10, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JPanel cashInfo = new JPanel();
        cashInfo.setLayout(new BoxLayout(cashInfo, BoxLayout.Y_AXIS));
        cashInfo.setOpaque(false);

        JPanel cashRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        cashRow.setOpaque(false);
        JLabel cashTitle = new JLabel("MealFlow Cash");
        cashTitle.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        cashTitle.setForeground(COLOR_ON_SURFACE);
        JLabel cashBadge = new JLabel(" ₹460 available ");
        cashBadge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        cashBadge.setOpaque(true);
        cashBadge.setBackground(COLOR_SECONDARY_FIXED);
        cashBadge.setForeground(COLOR_ON_SECONDARY_FIXED);
        cashBadge.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        cashRow.add(cashTitle);
        cashRow.add(cashBadge);

        JLabel cashSub = new JLabel("Fast 1-click redemption on daily culinary orders");
        cashSub.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        cashSub.setForeground(COLOR_ON_SURFACE_VARIANT);

        cashInfo.add(cashRow);
        cashInfo.add(Box.createVerticalStrut(2));
        cashInfo.add(cashSub);

        mealFlowCashCheck.setSelected(true);
        mealFlowCashCheck.setOpaque(false);
        mealFlowCashCheck.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        mealFlowCashCheck.setForeground(COLOR_ON_SURFACE);
        mealFlowCashCheck.addActionListener(e -> {
            recalculateBill();
            showToast(mealFlowCashCheck.isSelected() ? "Applied ₹200 MealFlow Cash" : "Removed MealFlow Cash");
        });

        cashWidget.add(cashInfo, BorderLayout.CENTER);
        cashWidget.add(mealFlowCashCheck, BorderLayout.EAST);
        card.add(cashWidget);
        card.add(Box.createVerticalStrut(14));

        // Payment Mode Radio Group
        ButtonGroup payGroup = new ButtonGroup();
        upiRadio.setSelected(true);
        payGroup.add(upiRadio);
        payGroup.add(cardRadio);
        payGroup.add(codRadio);

        // 1. UPI Method Card (Recommended)
        JPanel upiCard = new JPanel(new BorderLayout(8, 8));
        upiCard.setBackground(COLOR_SURFACE_LOW);
        upiCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SECONDARY, 12, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JPanel upiTop = new JPanel(new BorderLayout());
        upiTop.setOpaque(false);

        JPanel upiTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        upiTitleRow.setOpaque(false);
        upiRadio.setOpaque(false);
        JLabel upiName = new JLabel("UPI (Instant Settlement)");
        upiName.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        upiName.setForeground(COLOR_ON_SURFACE);

        JLabel upiTag = new JLabel(" RECOMMENDED ");
        upiTag.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        upiTag.setOpaque(true);
        upiTag.setBackground(COLOR_SECONDARY_FIXED);
        upiTag.setForeground(COLOR_ON_SECONDARY_FIXED);
        upiTag.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        upiTitleRow.add(upiRadio);
        upiTitleRow.add(upiName);
        upiTitleRow.add(upiTag);

        JLabel zeroFee = new JLabel("Zero Fee");
        zeroFee.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        zeroFee.setForeground(COLOR_ON_SURFACE_VARIANT);

        upiTop.add(upiTitleRow, BorderLayout.WEST);
        upiTop.add(zeroFee, BorderLayout.EAST);

        JLabel upiDesc = new JLabel("Google Pay, PhonePe, Paytm, BHIM & CRED");
        upiDesc.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        upiDesc.setForeground(COLOR_OUTLINE);
        upiDesc.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 0));

        // Verified VPA input row
        JPanel vpaRow = new JPanel(new BorderLayout(8, 0));
        vpaRow.setOpaque(false);
        vpaRow.setBorder(BorderFactory.createEmptyBorder(6, 28, 0, 0));

        upiIdField.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        upiIdField.setForeground(COLOR_ON_SURFACE);
        upiIdField.setBackground(COLOR_SURFACE_LOWEST);
        upiIdField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JButton changeVpaBtn = new JButton("Change VPA");
        changeVpaBtn.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        changeVpaBtn.setBackground(COLOR_SURFACE_HIGH);
        changeVpaBtn.setForeground(COLOR_ON_SURFACE);
        changeVpaBtn.setFocusPainted(false);
        changeVpaBtn.setBorder(new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1));
        changeVpaBtn.addActionListener(e -> {
            String newVpa = JOptionPane.showInputDialog(this, "Enter UPI ID (e.g. name@bank):", upiIdField.getText());
            if (newVpa != null && !newVpa.trim().isEmpty()) {
                upiIdField.setText(newVpa.trim());
                showToast("UPI VPA updated!");
            }
        });

        vpaRow.add(upiIdField, BorderLayout.CENTER);
        vpaRow.add(changeVpaBtn, BorderLayout.EAST);

        upiCard.add(upiTop, BorderLayout.NORTH);
        upiCard.add(upiDesc, BorderLayout.CENTER);
        upiCard.add(vpaRow, BorderLayout.SOUTH);
        card.add(upiCard);
        card.add(Box.createVerticalStrut(10));

        // 2. Credit / Debit Cards
        JPanel cardTile = new JPanel(new BorderLayout(10, 0));
        cardTile.setBackground(COLOR_SURFACE_LOWEST);
        cardTile.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 12, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JPanel cardLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cardLeft.setOpaque(false);
        cardRadio.setOpaque(false);
        JLabel visaBadge = new JLabel(" VISA ");
        visaBadge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        visaBadge.setOpaque(true);
        visaBadge.setBackground(COLOR_SURFACE_CONTAINER);
        visaBadge.setForeground(COLOR_ON_SURFACE_VARIANT);
        visaBadge.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JPanel cardText = new JPanel();
        cardText.setLayout(new BoxLayout(cardText, BoxLayout.Y_AXIS));
        cardText.setOpaque(false);

        JLabel cardTitle = new JLabel("HDFC Bank Debit Card •••• " + cardLast4);
        cardTitle.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        cardTitle.setForeground(COLOR_ON_SURFACE);

        JLabel cardSub = new JLabel("Expires " + cardExpiry + " • " + cardHolder);
        cardSub.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        cardSub.setForeground(COLOR_OUTLINE);

        cardText.add(cardTitle);
        cardText.add(cardSub);

        cardLeft.add(cardRadio);
        cardLeft.add(visaBadge);
        cardLeft.add(cardText);

        JButton newCardBtn = new JButton("+ New Card");
        newCardBtn.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        newCardBtn.setForeground(COLOR_PRIMARY);
        newCardBtn.setContentAreaFilled(false);
        newCardBtn.setBorderPainted(false);
        newCardBtn.setFocusPainted(false);
        newCardBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newCardBtn.addActionListener(e -> showAddCardDialog());

        cardTile.add(cardLeft, BorderLayout.CENTER);
        cardTile.add(newCardBtn, BorderLayout.EAST);
        card.add(cardTile);
        card.add(Box.createVerticalStrut(10));

        // 3. Cash on Delivery
        JPanel codTile = new JPanel(new BorderLayout(10, 0));
        codTile.setBackground(COLOR_SURFACE_LOWEST);
        codTile.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 12, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JPanel codLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        codLeft.setOpaque(false);
        codRadio.setOpaque(false);

        JPanel codText = new JPanel();
        codText.setLayout(new BoxLayout(codText, BoxLayout.Y_AXIS));
        codText.setOpaque(false);

        JLabel codTitle = new JLabel("Cash / UPI on Delivery");
        codTitle.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        codTitle.setForeground(COLOR_ON_SURFACE);

        JLabel codSub = new JLabel("Pay via cash or dynamic QR code during doorstep handover");
        codSub.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        codSub.setForeground(COLOR_OUTLINE);

        codText.add(codTitle);
        codText.add(codSub);

        codLeft.add(codRadio);
        codLeft.add(codText);

        JLabel availBadge = new JLabel(" Available ");
        availBadge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        availBadge.setOpaque(true);
        availBadge.setBackground(COLOR_SURFACE_CONTAINER);
        availBadge.setForeground(COLOR_ON_SURFACE_VARIANT);
        availBadge.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        codTile.add(codLeft, BorderLayout.CENTER);
        codTile.add(availBadge, BorderLayout.EAST);
        card.add(codTile);

        return card;
    }

    // -------------------------------------------------------------------------
    // RIGHT CHECKOUT COLUMN (ORDER SUMMARY & BILL DETAILS)
    // -------------------------------------------------------------------------
    private JPanel createRightCheckoutColumn() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        // 1. Promo Code Panel
        right.add(createPromoPanel());
        right.add(Box.createVerticalStrut(18));

        // 2. Bill Details Card
        right.add(createBillDetailsCard());
        right.add(Box.createVerticalStrut(12));

        // 3. Support link
        JPanel helpRow = new JPanel(new BorderLayout());
        helpRow.setOpaque(false);
        JLabel helpQ = new JLabel("Need to modify order?");
        helpQ.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        helpQ.setForeground(COLOR_OUTLINE);

        JButton chatBtn = new JButton("Chat with Kitchen Support ↗");
        chatBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        chatBtn.setForeground(COLOR_PRIMARY);
        chatBtn.setContentAreaFilled(false);
        chatBtn.setBorderPainted(false);
        chatBtn.setFocusPainted(false);
        chatBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chatBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Connecting to The Green Kitchen head chef...", "Kitchen Support", JOptionPane.INFORMATION_MESSAGE));

        helpRow.add(helpQ, BorderLayout.WEST);
        helpRow.add(chatBtn, BorderLayout.EAST);
        right.add(helpRow);

        return right;
    }

    private JPanel createPromoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_SURFACE_LOWEST);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 16, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel promoTag = new JLabel("COUPONS & OFFERS");
        promoTag.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        promoTag.setForeground(COLOR_ON_SURFACE_VARIANT);
        panel.add(promoTag);
        panel.add(Box.createVerticalStrut(8));

        // Input row
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);

        couponInputField.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        couponInputField.setForeground(COLOR_ON_SURFACE);
        couponInputField.setBackground(COLOR_SURFACE_LOW);
        couponInputField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JButton applyBtn = new JButton("Apply");
        applyBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        applyBtn.setBackground(COLOR_PRIMARY);
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFocusPainted(false);
        applyBtn.setBorder(new RoundedBorder(COLOR_PRIMARY_CONTAINER, 8, 1));
        applyBtn.addActionListener(e -> {
            String code = couponInputField.getText().trim();
            if ("SAVEMF".equalsIgnoreCase(code) || "MEAL50".equalsIgnoreCase(code)) {
                isCouponApplied = true;
                couponActiveBadge.setVisible(true);
                recalculateBill();
                showToast("Coupon '" + code + "' applied! ₹50 saved.");
            } else {
                showToast("Invalid coupon code. Try 'SAVEMF'");
            }
        });

        inputRow.add(couponInputField, BorderLayout.CENTER);
        inputRow.add(applyBtn, BorderLayout.EAST);
        panel.add(inputRow);
        panel.add(Box.createVerticalStrut(8));

        // Active coupon badge
        couponActiveBadge = new JPanel(new BorderLayout());
        couponActiveBadge.setBackground(new Color(0xD8, 0xF5, 0xE5));
        couponActiveBadge.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SECONDARY_CONTAINER, 8, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JLabel cText = new JLabel("✓ 'SAVEMF' applied (₹50 discount)");
        cText.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        cText.setForeground(COLOR_SECONDARY);

        JButton removeCoupon = new JButton("Remove");
        removeCoupon.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        removeCoupon.setForeground(Color.RED);
        removeCoupon.setContentAreaFilled(false);
        removeCoupon.setBorderPainted(false);
        removeCoupon.setFocusPainted(false);
        removeCoupon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeCoupon.addActionListener(e -> {
            isCouponApplied = false;
            couponActiveBadge.setVisible(false);
            recalculateBill();
            showToast("Removed coupon discount.");
        });

        couponActiveBadge.add(cText, BorderLayout.WEST);
        couponActiveBadge.add(removeCoupon, BorderLayout.EAST);
        panel.add(couponActiveBadge);

        return panel;
    }

    private JPanel createBillDetailsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_SURFACE_LOWEST);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 16, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel billTitle = new JLabel("Bill Details");
        billTitle.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        billTitle.setForeground(COLOR_ON_SURFACE);

        JLabel kitchenNote = new JLabel("3 items • The Green Kitchen");
        kitchenNote.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        kitchenNote.setForeground(COLOR_OUTLINE);

        cardHeader.add(billTitle, BorderLayout.WEST);
        cardHeader.add(kitchenNote, BorderLayout.EAST);
        card.add(cardHeader);
        card.add(Box.createVerticalStrut(14));

        // Breakdown lines
        subtotalLabel = new JLabel("₹575.00");
        packagingLabel = new JLabel("₹30.00");
        deliveryFeeLabel = new JLabel("₹45.00");
        gstLabel = new JLabel("₹32.50");
        discountLabel = new JLabel("-₹50.00");
        cashDiscountLabel = new JLabel("-₹200.00");

        card.add(createBillLine("Item Subtotal", subtotalLabel, false));
        card.add(Box.createVerticalStrut(6));
        card.add(createBillLine("Restaurant Packaging", packagingLabel, false));
        card.add(Box.createVerticalStrut(6));
        card.add(createBillLine("Delivery Partner Fee (3.2 km)", deliveryFeeLabel, false));
        card.add(Box.createVerticalStrut(6));
        card.add(createBillLine("Taxes & Govt GST (5%)", gstLabel, false));
        card.add(Box.createVerticalStrut(6));
        card.add(createBillLine("Promo Code Discount (SAVEMF)", discountLabel, true));
        card.add(Box.createVerticalStrut(6));
        card.add(createBillLine("MealFlow Cash Applied", cashDiscountLabel, true));
        card.add(Box.createVerticalStrut(14));

        // Estimated Nutrition Sparkline Gauge
        card.add(createNutritionGauge());
        card.add(Box.createVerticalStrut(14));

        // Grand Total Row
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);

        JPanel leftTotal = new JPanel();
        leftTotal.setLayout(new BoxLayout(leftTotal, BoxLayout.Y_AXIS));
        leftTotal.setOpaque(false);

        JLabel toPay = new JLabel("To Pay");
        toPay.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        toPay.setForeground(COLOR_ON_SURFACE);

        savingsLabel = new JLabel("Total Savings: ₹250.00");
        savingsLabel.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        savingsLabel.setForeground(COLOR_SECONDARY);

        leftTotal.add(toPay);
        leftTotal.add(savingsLabel);

        grandTotalLabel = new JLabel("₹482.50");
        grandTotalLabel.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        grandTotalLabel.setForeground(COLOR_PRIMARY);

        totalRow.add(leftTotal, BorderLayout.WEST);
        totalRow.add(grandTotalLabel, BorderLayout.EAST);
        card.add(totalRow);
        card.add(Box.createVerticalStrut(16));

        // Big Primary CTA Button
        placeOrderBtn = new JButton("🔒 Place Order • ₹482.50");
        placeOrderBtn.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        placeOrderBtn.setBackground(COLOR_PRIMARY);
        placeOrderBtn.setForeground(Color.WHITE);
        placeOrderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        placeOrderBtn.setFocusPainted(false);
        placeOrderBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        placeOrderBtn.setBorder(new RoundedBorder(COLOR_PRIMARY_CONTAINER, 12, 1));
        placeOrderBtn.addActionListener(e -> executeOrderAndPayment());
        card.add(placeOrderBtn);
        card.add(Box.createVerticalStrut(8));

        JLabel terms = new JLabel("<html><center><font color='#8d706b' size='-2'>By placing this order you agree to MealFlow's Terms & Food Quality Standard.</font></center></html>", SwingConstants.CENTER);
        terms.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(terms);

        return card;
    }

    private JPanel createBillLine(String title, JLabel valueLabel, boolean isGreen) {
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        t.setForeground(isGreen ? COLOR_SECONDARY : COLOR_ON_SURFACE_VARIANT);

        valueLabel.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        valueLabel.setForeground(isGreen ? COLOR_SECONDARY : COLOR_ON_SURFACE);

        line.add(t, BorderLayout.WEST);
        line.add(valueLabel, BorderLayout.EAST);
        return line;
    }

    private JPanel createNutritionGauge() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_SURFACE_LOW);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 8, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Total Estimated Nutrition");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        title.setForeground(COLOR_ON_SURFACE);
        JLabel cals = new JLabel("840 kcal");
        cals.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        cals.setForeground(COLOR_SECONDARY);
        top.add(title, BorderLayout.WEST);
        top.add(cals, BorderLayout.EAST);
        panel.add(top);
        panel.add(Box.createVerticalStrut(6));

        // Multi-segment sparkline bar: 45% Protein (Green), 35% Carbs (Orange), 20% Fats (Red)
        JPanel sparkline = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(COLOR_SECONDARY);
                g2.fillRoundRect(0, 0, (int) (w * 0.45), h, 4, 4);

                g2.setColor(COLOR_TERTIARY);
                g2.fillRect((int) (w * 0.45), 0, (int) (w * 0.35), h);

                g2.setColor(COLOR_PRIMARY);
                g2.fillRoundRect((int) (w * 0.80), 0, w - (int) (w * 0.80), h, 4, 4);

                g2.dispose();
            }
        };
        sparkline.setPreferredSize(new Dimension(240, 8));
        sparkline.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        panel.add(sparkline);
        panel.add(Box.createVerticalStrut(6));

        // Macros breakdown row
        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tags.setOpaque(false);
        tags.add(createMiniTag("● 38g Protein", COLOR_SECONDARY));
        tags.add(createMiniTag("● 54g Carbs", COLOR_TERTIARY));
        tags.add(createMiniTag("● 18g Fats", COLOR_PRIMARY));
        panel.add(tags);

        return panel;
    }

    private JLabel createMiniTag(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        l.setForeground(color);
        return l;
    }

    // =========================================================================
    // DYNAMIC BILL CALCULATION
    // =========================================================================
    private double recalculateBill() {
        double subtotal = 0;
        int totalItems = 0;
        for (CheckoutCartItem item : cartItems) {
            subtotal += item.unitPrice * item.quantity;
            totalItems += item.quantity;
        }

        double packaging = 30.0;
        double deliveryFee = 45.0;
        double gst = Math.round((subtotal * 0.05) * 100.0) / 100.0;

        double promoDiscount = isCouponApplied ? 50.0 : 0.0;
        double cashDiscount = mealFlowCashCheck.isSelected() ? 200.0 : 0.0;

        double grandTotal = Math.max(0.0, (subtotal + packaging + deliveryFee + gst) - (promoDiscount + cashDiscount));
        double totalSavings = promoDiscount + cashDiscount;

        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("₹%.2f", subtotal));
            packagingLabel.setText(String.format("₹%.2f", packaging));
            deliveryFeeLabel.setText(String.format("₹%.2f", deliveryFee));
            gstLabel.setText(String.format("₹%.2f", gst));
            discountLabel.setText(String.format("-₹%.2f", promoDiscount));
            cashDiscountLabel.setText(String.format("-₹%.2f", cashDiscount));
            grandTotalLabel.setText(String.format("₹%.2f", grandTotal));
            savingsLabel.setText(String.format("Total Savings: ₹%.2f", totalSavings));
            placeOrderBtn.setText(String.format("🔒 Place Order • ₹%.2f", grandTotal));
            cartCountHeader.setText(String.format("🛍 Cart (%d) • ₹%.0f", totalItems, subtotal));
        }

        return grandTotal;
    }

    // =========================================================================
    // ORDER EXECUTION & OOP PAYMENT INTEGRATION
    // =========================================================================
    private void executeOrderAndPayment() {
        double amount = recalculateBill();

        // 1. Instantiate the Polymorphic Payment implementation based on user choice
        Payment payment;
        if (upiRadio.isSelected()) {
            String vpa = upiIdField.getText().trim();
            if (vpa.isEmpty()) vpa = "rahul.sharma@okaxis";
            payment = new UPIPayment(amount, vpa);
        } else if (cardRadio.isSelected()) {
            payment = new CardPayment(amount, "411122223333" + cardLast4, cardHolder, cardExpiry, "123");
        } else {
            payment = new CODPayment(amount, selectedAddressText);
        }

        // Execute payment logic
        boolean isSuccess = payment.pay();

        // 2. Persist order into MySQL / In-memory fallback via OrderService
        if (isSuccess) {
            Customer user = AuthService.getCurrentUser();
            int customerId = (user != null && user.getId() > 0) ? user.getId() : 1;

            java.util.Map<Integer, Integer> itemQuantities = new java.util.HashMap<>();
            for (CheckoutCartItem item : cartItems) {
                itemQuantities.put(item.itemId, item.quantity);
            }

            try {
                orderService.checkout(customerId, itemQuantities, selectedAddressText, customerPhone, payment.getMethodName());
            } catch (Exception ex) {
                System.err.println("[PaymentPage] DB order record skipped (offline mode active): " + ex.getMessage());
            }
        }

        // 3. Launch PaymentResultPage
        new PaymentResultPage(payment);
        dispose();
    }

    private void showAddCardDialog() {
        JTextField numField = new JTextField(16);
        JTextField nameField = new JTextField(customerName);
        JTextField expField = new JTextField("12/28");
        JPasswordField cvvField = new JPasswordField(3);

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Card Number (16 digits):"));
        p.add(numField);
        p.add(new JLabel("Cardholder Name:"));
        p.add(nameField);
        p.add(new JLabel("Expiry (MM/YY):"));
        p.add(expField);
        p.add(new JLabel("CVV:"));
        p.add(cvvField);

        int result = JOptionPane.showConfirmDialog(this, p, "Add Credit/Debit Card", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION && numField.getText().trim().length() >= 4) {
            String fullNum = numField.getText().trim();
            cardLast4 = fullNum.substring(Math.max(0, fullNum.length() - 4));
            cardHolder = nameField.getText().trim();
            cardExpiry = expField.getText().trim();
            cardRadio.setSelected(true);
            showToast("Added Card ending with •••• " + cardLast4);
        }
    }

    private void showWeeklyScheduleDialog() {
        JOptionPane.showMessageDialog(this,
                "<html><body style='width:340px; font-family:sans-serif;'>"
                        + "<h3 style='color:#006c4a;'>Weekly Delivery Schedule</h3>"
                        + "<p><b>Active Plan:</b> Balanced Week (3 Meals/Day)</p>"
                        + "<ul><li>Lunch: 1:00 PM</li><li>Dinner: 8:00 PM</li></ul>"
                        + "<p>Pause or edit up to 4 hours prior.</p></body></html>",
                "Weekly Schedule", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSubscriptionsDialog() {
        JOptionPane.showMessageDialog(this,
                "<html><body style='width:340px; font-family:sans-serif;'>"
                        + "<h3 style='color:#ab2e16;'>My Subscriptions</h3>"
                        + "<p><b>Plan:</b> Balanced Week Cooked Subscription<br>"
                        + "<b>Vendor:</b> The Green Kitchen & Annapurna<br>"
                        + "<b>Auto-Renewal:</b> Enabled</p></body></html>",
                "Subscriptions", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // FLOATING TOAST HELPER
    // =========================================================================
    private void setupFloatingToast() {
        toastPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        toastPanel.setBackground(COLOR_INVERSE_SURFACE);
        toastPanel.setBorder(new RoundedBorder(new Color(0x44, 0x50, 0x47), 12, 1));
        toastPanel.setVisible(false);

        toastLabel = new JLabel("Added to cart successfully");
        toastLabel.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        toastLabel.setForeground(Color.WHITE);
        toastPanel.add(toastLabel);

        JLayeredPane lp = getLayeredPane();
        toastPanel.setBounds(460, 760, 360, 42);
        lp.add(toastPanel, JLayeredPane.POPUP_LAYER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                toastPanel.setBounds((getWidth() - 360) / 2, getHeight() - 100, 360, 42);
            }
        });
    }

    public void showToast(String message) {
        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }
        toastLabel.setText("  ✓  " + message);
        toastPanel.setVisible(true);
        toastTimer = new Timer(2500, e -> toastPanel.setVisible(false));
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    // =========================================================================
    // INNER CLASSES: CART ITEM MODEL & BORDER
    // =========================================================================
    public static class CheckoutCartItem {
        public int itemId;
        public String name;
        public String addons;
        public String prepNotes;
        public double unitPrice;
        public int quantity;
        public String imageKey;

        public CheckoutCartItem(int itemId, String name, String addons, String prepNotes, double unitPrice, int quantity) {
            this(itemId, name, addons, prepNotes, unitPrice, quantity, "");
        }

        public CheckoutCartItem(int itemId, String name, String addons, String prepNotes, double unitPrice, int quantity, String imageKey) {
            this.itemId = itemId;
            this.name = name;
            this.addons = addons;
            this.prepNotes = prepNotes;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.imageKey = imageKey;
        }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PaymentPage("Rahul", 482.50).setVisible(true));
    }
}
