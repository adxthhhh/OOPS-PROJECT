package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.Customer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExplorePage - High-fidelity Java Swing implementation of the MealFlow Explore dashboard.
 * Faithfully mirrors the HTML/Tailwind design language, colors, typography, and interactive components.
 */
public class ExplorePage extends JFrame {

    // =========================================================================
    // COLOR PALETTE (From HTML Tailwind Config)
    // =========================================================================
    public static final Color COLOR_BG                 = new Color(0xF3, 0xFC, 0xF3); // #f3fcf3
    public static final Color COLOR_SURFACE            = new Color(0xFF, 0xFF, 0xFF); // #ffffff
    public static final Color COLOR_SURFACE_LOW        = new Color(0xED, 0xF6, 0xEE); // #edf6ee
    public static final Color COLOR_SURFACE_CONTAINER  = new Color(0xE7, 0xF0, 0xE8); // #e7f0e8
    public static final Color COLOR_SURFACE_HIGH       = new Color(0xE2, 0xEA, 0xE2); // #e2eae2
    public static final Color COLOR_SURFACE_HIGHEST    = new Color(0xDC, 0xE5, 0xDD); // #dce5dd

    public static final Color COLOR_PRIMARY            = new Color(0xAB, 0x2E, 0x16); // #ab2e16 (Warm terracotta)
    public static final Color COLOR_PRIMARY_CONTAINER  = new Color(0xCE, 0x46, 0x2C); // #ce462c
    public static final Color COLOR_PRIMARY_FIXED      = new Color(0xFF, 0xDA, 0xD3); // #ffdad3
    public static final Color COLOR_ON_PRIMARY_FIXED   = new Color(0x3E, 0x04, 0x00); // #3e0400

    public static final Color COLOR_SECONDARY          = new Color(0x00, 0x6C, 0x4A); // #006c4a (Emerald green)
    public static final Color COLOR_SECONDARY_CONTAINER= new Color(0x82, 0xF5, 0xC1); // #82f5c1
    public static final Color COLOR_ON_SEC_CONTAINER   = new Color(0x00, 0x71, 0x4E); // #00714e
    public static final Color COLOR_SECONDARY_FIXED    = new Color(0x85, 0xF8, 0xC4); // #85f8c4
    public static final Color COLOR_ON_SEC_FIXED_VAR   = new Color(0x00, 0x51, 0x37); // #005137

    public static final Color COLOR_TERTIARY           = new Color(0xA3, 0x39, 0x00); // #a33900
    public static final Color COLOR_TERTIARY_FIXED     = new Color(0xFF, 0xDB, 0xCE); // #ffdbce

    public static final Color COLOR_TEXT_ON_SURFACE    = new Color(0x15, 0x1D, 0x19); // #151d19
    public static final Color COLOR_TEXT_MUTED         = new Color(0x59, 0x41, 0x3C); // #59413c
    public static final Color COLOR_OUTLINE            = new Color(0x8D, 0x70, 0x6B); // #8d706b
    public static final Color COLOR_OUTLINE_VARIANT    = new Color(0xE1, 0xBF, 0xB8); // #e1bfb8
    public static final Color COLOR_AMBER_STAR         = new Color(0xF5, 0x9E, 0x0B); // #f59e0b

    // Font family helper
    private static final String FONT_NAME = getSafeFontFamily();

    // =========================================================================
    // STATE & SERVICES
    // =========================================================================
    protected final String customerName;
    private final Map<String, Integer> cartItems = new LinkedHashMap<>();
    private final Set<String> activeFilters = new HashSet<>();
    private String selectedCategory = null;
    private String searchQuery = "";

    // Header widgets
    private JButton cartBtn;
    private JLabel locationLabel;
    private JTextField searchField;

    // Content container for dishes
    private JPanel dishesGridPanel;
    private List<DishCardComponent> dishCardComponents = new ArrayList<>();
    private List<KitchenCardComponent> kitchenCardComponents = new ArrayList<>();

    // Async Image Cache
    private static final Map<String, BufferedImage> imageCache = new ConcurrentHashMap<>();
    private static final ExecutorService imagePool = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "ImageLoaderPool");
        t.setDaemon(true);
        return t;
    });

    // Data lists
    private List<Dish> allDishes = new ArrayList<>();
    private List<Kitchen> allKitchens = new ArrayList<>();
    private List<ScheduleItem> scheduleItems = new ArrayList<>();

    public ExplorePage(String customerName) {
        this.customerName = (customerName != null && !customerName.trim().isEmpty())
                ? customerName.trim()
                : "Rahul";

        setTitle("MealFlow - Explore Fresh Neighborhood Kitchens & Meal Plans");
        setSize(1320, 860);
        setMinimumSize(new Dimension(1020, 680));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initSampleData();
        buildUI();
    }

    // =========================================================================
    // DATA MODEL INITIALIZATION
    // =========================================================================
    private void initSampleData() {
        // Pre-populate default cart matching the HTML ("Cart (3) • ₹460")
        // Dish 1: 1 x Paneer Tikka (240), Dish 2: 1 x Smoked Chicken Wrap (220) = 460
        cartItems.put("Paneer Tikka Brown Rice Bowl", 1);
        cartItems.put("Smoked Chicken Multigrain Wrap", 1);

        allDishes.add(new Dish(
                "Paneer Tikka Brown Rice Bowl",
                "The Green Kitchen",
                4.8, 320,
                true, false,
                "25-30m",
                28, 480, "High Fiber",
                240,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAmYT8BUvvrf0xyZ7cGWw71hGcUKrvV_SFjHGvY6xgixJlDJ9o_oMcUPaz562Fhj8hB0f6UtUiGKbsSMWSP_yynNmUHNS4IchS3VZ4Vz8Tei_Mbw_AmXsJNzJ5gQeeMVoEiLlrGWEQnE8ZSfLwmzYDHxwWsraPsIthXFQJyNy3OXPpvgrpCJQ_zRvNJ1bguKiHmhv8uXgi2Chxnm6x8E_qrc-KX_Q8d09Gds4Cbc7MJJXFdSbI2Vh25",
                "Lunch Bowls"
        ));

        allDishes.add(new Dish(
                "Smoked Chicken Multigrain Wrap",
                "Lean Crust Deli",
                4.7, 490,
                false, false,
                "20-25m",
                34, 410, "Low Carb",
                220,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuB09XxCb5P6sa41nZA-42qvf8X4qcGG8bqkDCg-dEduta_d_bIT4bL1m5fl8nKVOBib4TrWvTcGXMH4BCwRKfnxInXDT9OVI0xnRnDjdGLLbKOsHfme-qMbAiqYrCU2FgoXb7BgT9Zr1hCKPknkR4ROvssg3FokyGnwnNV1_dSD_gRX13R92vgL9R1PzPLblRKDnhq8zNkK8qEe6NTwNWbwACdYtA6aPrCbjkNftHk7wFiZHT4pSc_Y",
                "High Protein"
        ));

        allDishes.add(new Dish(
                "South Indian Breakfast Thali",
                "Dakshin Flavors",
                4.9, 810,
                true, false,
                "20m",
                14, 390, "Steamed",
                160,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDZikScSrhbwHd90JnhPcJ7hmnJWCeZng08c1ETs4vPXfzve-7TCKLSW20QrlXGwEASB1pYtzhE3zZ1uMIh_Fhy0zNoa0D1tW05maXMAMbw-HxNwFzngOf2PY50Aq2MAGBqV9p6CObhtFDBs4ilevi_XC5fjFbffy2bASOc_aQdScN6FMPbxFjtWRTyuFnDdhvZwEovRIMzlxyQ02UZUiEe2XnFQ_TdhdWi1sk9OpmcuxNqm8bzsYtD",
                "Breakfast"
        ));

        allDishes.add(new Dish(
                "Dal Makhani & Jeera Rice Meal",
                "Ghar Ka Swad Cloud Kitchen",
                4.6, 1100,
                true, false,
                "35m",
                18, 520, "Homestyle",
                190,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuD1WCDOf8IzhiOYHzaCgaGvnxBh12wZUuPkh5PrFco1s37KmYDJlTbbVvlKHl9bJ_PeumM2GJc5MQmwZQ5gtLnjbP-wZ3AONOStFxC8E__3z4aPnwlFzn0Lo6OSQ0xr4o2za9MhLBcCh0JciaR1khEWYnMoSpbuiPdzNTmcVHDhSXgrGdKnSF4-iUhSAKgRIy7jwF2FduAGX2PgAkl42JIJA3bSyC1CJxhzhXa1sv1oeE9fA7MtMhJy",
                "Thali & Curries"
        ));

        allDishes.add(new Dish(
                "Grilled Tofu & Edamame Quinoa Salad",
                "FreshRoots Organic",
                4.8, 275,
                true, true,
                "25m",
                22, 360, "Zero Sugar",
                260,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDDLaJa-Qjyq9z9X5Nh_cfGDbIIIigVFT5CymKQMuV4uSpoi9dXqa2cDZ00wPVN6o51e7RuoqsxcgARTLBpRbVlB0fyzlzlYxA7_pq74YSeiAegp_Y5orJ7UlXmMIQhfSEeBI4SrHK0uXIkYpcJsHgQmO6dGn0AkGToy6KZZQeOLapef0Oukx9meJXUCN0v2KbHIIoI6hyS8VmAm9Jq18WR_a_gtKaz9LeVrP5si07slN-Xk2kgFE1B",
                "Salads & Wraps"
        ));

        allDishes.add(new Dish(
                "Homestyle Chicken Curry & Millet Rotis",
                "Annapurna Tiffins",
                4.7, 620,
                false, false,
                "30m",
                32, 490, "Gluten-Free Roti",
                250,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuArEx_9E9w6TLTJxqZjdytT5hkgXPZpCpg6RRYbINQt611UphSQx8dz3KZD6Z1oZTbarL7fLhHGFmeIc8p-B6_GM6sp3qiwMLa1Ii6C-8XTqOI3ty0_1VHTbnePV_X3DzIleoEq3ZBIIWBB9UFnCaVc041bfncJ6G-2OAtHjOnpqG2gspDC9OsdcRfTdxxJ5sy3jMLcSlzL9qpM2CuFXKvjhnFoe95UYP-_fyzetp3uXEkkegMV3cQ8",
                "Thali & Curries"
        ));

        // Recommended Items
        scheduleItems.add(new ScheduleItem(
                "Cottage Cheese & Quinoa Medley",
                "31g Protein", 210, "Prep time: 15 mins",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAKlPuLS70uMliQnGgleMghO8IE1LnjKZae8dy7j9907bYRyoxQTDR0oEq3pWkVWFejviH2qs5CHRUxJTL7PqYe8KZ9-0UEbn0ZQbFs3-XbPP7J4ZImZeH7nRqvo98dL-XytIjqHkMv_5i0v-RNPp2XTL3PhRowXRCvbWDxN53JOGHHrvR3a87Y-CW17copiT0Nxz1b8BaReaqcm1pkfEOjkFiJ_zJvyqCep4OqynBOdBcJhWAy14AG"
        ));
        scheduleItems.add(new ScheduleItem(
                "Charred Lemon Herb Chicken",
                "38g Protein", 260, "FitFuel Bangalore",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuBae3KYyshQ9VH_pTEQys5DPyYjWXnVtDSELZMheUFvHcqqU7Ww_ER9zq2BsKvmOTTyDpJHABbnKUkJwFMyH5Gqz0-qzJTAWlTIVLI-vYQ--qCxqGrPo8ozhov8MnJ_w0duoDiQuMOkBR7nbl8-H1oMhu0oBeA5O7JkSvx0cA0udYKrUjE30Mo3xV_Ok-UKKewy1ecy3fk37_Rg3LTz0iLWpNV5z-z87oFkJLG6G6KI3ZYS27lyz93S"
        ));
        scheduleItems.add(new ScheduleItem(
                "Warm Kala Chana Protein Salad",
                "19g Protein", 140, "Southern Spoon Co.",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuADsoHsdtGlj-vE3YahAFzxQHTBZXMmxQXglUsUcBSJh_dL3Dg-Bu4crhrMNTTav2ObALFiOaFUoE4jkm544J9Qu5XAhOXOxHLy94S_Ym8CBVCNykZ5csgfwlfhmhfe_3aDw3C8tDwdBwmWt6kWFieg9NGaSrrNVBUdiXnqSQFFdt0SIs4FZqMHZ7iBRjoZWIDVxgr55r0f0qP1k4iMCDHprNBrvnWOiV8zwUxmHsSo26P5-3cHcfsi"
        ));

        // Featured Kitchens
        allKitchens.add(new Kitchen(
                "The Green Kitchen",
                "Certified Meal Partner",
                4.8, 1200,
                "Healthy Bowls, Quinoa Platters & Cold-Pressed Juices",
                "25-30 mins", 180, "Subscriptions Available",
                COLOR_SECONDARY,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuD-EMNpfjfIgv2QTOxIk1IUm3niAw9PevM7USLNKCrTRAAPXeWBCAjsYX7u-Qnq7pKk43HRl99AkDmJrF9wSWtlgEc6-_wTu2sDeKPChDOu677ZAk1HU6lPbjUJkA2zbLJVpxn54uAhPml9xPmJJqWo_4mA4u8Xt1fcsjWXr9jUFutbqcT_AbdUFXG89lVUOmha8sSRmRClAXxwnKAVF0OCcHfXPSjTOhfN5jY-UloZ_FsM1L0mr5oQ"
        ));
        allKitchens.add(new Kitchen(
                "Annapurna Homestyle Tiffins",
                "Popular Subscription Kitchen",
                4.9, 850,
                "Daily Indian Lunch & Dinner Rotational Menus",
                "20-25 mins", 140, "Zero Oil Options",
                COLOR_TERTIARY,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDC4HU9aV6pbepPKxPNQY2vw3ZeF7hzBW1O12XVftVaf_RQskPFZbA0HzcZoQ-MsP7u9TFYOosPbPaL8LVABDFb6TigU9WYq1HhJ6dBZzdF4zXXtlCbXwTgKtVoxaxgO2VOpoc5uFHf13yDef8vmop4LLqv15Hac0RUBuwrYM25UzCrNJHc-PDWbFCxueqtdEtN10pFdC-TGF2mClfmMcggxdAc7XCoEH6ygVghWU-nvAi1mGPyVaSO"
        ));
        allKitchens.add(new Kitchen(
                "Lean Crust Deli",
                "Fitness Favorite",
                4.7, 640,
                "High-Protein Gourmet Sandwiches, Wraps & Energy Bowls",
                "30 mins", 220, "Calorie Counted",
                COLOR_PRIMARY,
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDMchJgsD6-XQCShUCASDxKlDw4v69hs8qpcjcNj8B1rNZzIU66Av4EsOmGw9pelueIcqBmdab1PoN0wQjII9T9tugJ6PcXlD_w9ihbXO_QFlnaXgspsMhqZSB2iMH4N6UH7aGux1oPpKwTziL3uTrupKY_gTa9478w7vQhlyDNg6Q0S-ggxKZC_kd6BYyk0gFfNjnmoBX3abOD4xmMRQZZTYpBELbfARkZlF-XznHQBfcLeXPr4AVK"
        ));
    }

    // =========================================================================
    // MAIN UI LAYOUT
    // =========================================================================
    private void buildUI() {
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // 1. Fixed Left Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Right Side: Top Header + Scrollable Main Content
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(COLOR_BG);

        JPanel header = createHeader();
        rightContainer.add(header, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(createScrollableMainContent());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        rightContainer.add(scrollPane, BorderLayout.CENTER);

        add(rightContainer, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. LEFT SIDEBAR COMPONENT
    // =========================================================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(COLOR_SURFACE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_SURFACE_HIGH));

        // Top brand logo + nav
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setBackground(COLOR_SURFACE);
        topArea.setBorder(BorderFactory.createEmptyBorder(20, 16, 16, 16));

        // Logo row
        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setBackground(COLOR_SURFACE);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoIcon = new JLabel(new LeafLogoIcon(26));
        JLabel brandName = new JLabel("MealFlow");
        brandName.setFont(new Font(FONT_NAME, Font.BOLD, 20));
        brandName.setForeground(COLOR_TEXT_ON_SURFACE);

        brandRow.add(logoIcon);
        brandRow.add(brandName);
        topArea.add(brandRow);
        topArea.add(Box.createVerticalStrut(24));

        // Nav items
        JPanel navList = new JPanel(new GridLayout(6, 1, 0, 4));
        navList.setBackground(COLOR_SURFACE);
        navList.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Explore (active)
        JButton exploreBtn = createNavButton("Explore", "explore", true);
        exploreBtn.addActionListener(e -> {
            // Already here; reset filters
            activeFilters.clear();
            selectedCategory = null;
            searchField.setText("");
            filterDishes();
        });

        // Meal Plans
        JButton mealPlansBtn = createNavButton("Meal Plans", "restaurant_menu", false);
        mealPlansBtn.addActionListener(e -> {
            new MealPlansPage(customerName).setVisible(true);
            dispose();
        });

        // Weekly Schedule
        JButton scheduleBtn = createNavButton("Weekly Schedule", "calendar_today", false);
        scheduleBtn.addActionListener(e -> showWeeklyScheduleDialog());

        // My Orders (Person 5 hook)
        JButton myOrdersBtn = createNavButton("My Orders", "receipt_long", false);
        myOrdersBtn.addActionListener(e -> {
            new MyOrdersPage(customerName).setVisible(true);
            dispose();
        });

        // My Subscriptions
        JButton subsBtn = createNavButton("My Subscriptions", "autorenew", false);
        subsBtn.addActionListener(e -> showSubscriptionsDialog());

        // Profile & Preferences
        JButton profileBtn = createNavButton("Profile & Preferences", "manage_accounts", false);
        profileBtn.addActionListener(e -> {
            new ProfilePage(customerName).setVisible(true);
            dispose();
        });

        navList.add(exploreBtn);
        navList.add(mealPlansBtn);
        navList.add(scheduleBtn);
        navList.add(myOrdersBtn);
        navList.add(subsBtn);
        navList.add(profileBtn);

        topArea.add(navList);
        sidebar.add(topArea, BorderLayout.NORTH);

        // Bottom area: Active Plan Box, Support, MVP footer
        JPanel bottomArea = new JPanel();
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBackground(COLOR_SURFACE);
        bottomArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_SURFACE_HIGH),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        // Active plan card
        JPanel activePlanCard = new JPanel(new BorderLayout(8, 4));
        activePlanCard.setBackground(COLOR_SURFACE_LOW);
        activePlanCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 8, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        activePlanCard.setMaximumSize(new Dimension(208, 64));
        activePlanCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel planHeaderRow = new JPanel(new BorderLayout());
        planHeaderRow.setBackground(COLOR_SURFACE_LOW);
        JLabel activePlanBadge = new JLabel("ACTIVE PLAN");
        activePlanBadge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        activePlanBadge.setForeground(COLOR_SECONDARY);

        JLabel greenDot = new JLabel(new IndicatorDotIcon(COLOR_SECONDARY, 8));
        planHeaderRow.add(activePlanBadge, BorderLayout.WEST);
        planHeaderRow.add(greenDot, BorderLayout.EAST);

        JLabel planTitle = new JLabel("Balanced Week");
        planTitle.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        planTitle.setForeground(COLOR_TEXT_ON_SURFACE);

        activePlanCard.add(planHeaderRow, BorderLayout.NORTH);
        activePlanCard.add(planTitle, BorderLayout.SOUTH);

        bottomArea.add(activePlanCard);
        bottomArea.add(Box.createVerticalStrut(12));

        // Support & Help button
        JButton supportBtn = new JButton("Support & Help");
        supportBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        supportBtn.setForeground(COLOR_TEXT_MUTED);
        supportBtn.setContentAreaFilled(false);
        supportBtn.setBorderPainted(false);
        supportBtn.setFocusPainted(false);
        supportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        supportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        supportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "MealFlow 24/7 Concierge Support\nPhone: +91 80 4920 1100\nEmail: support@mealflow.in",
                "Customer Support", JOptionPane.INFORMATION_MESSAGE));
        bottomArea.add(supportBtn);
        bottomArea.add(Box.createVerticalStrut(8));

        // Version & platform
        JPanel verRow = new JPanel(new BorderLayout());
        verRow.setBackground(COLOR_SURFACE);
        verRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel vLbl = new JLabel("v1.0 MVP");
        vLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        vLbl.setForeground(COLOR_OUTLINE);
        JLabel dLbl = new JLabel("Desktop App");
        dLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        dLbl.setForeground(COLOR_OUTLINE);
        verRow.add(vLbl, BorderLayout.WEST);
        verRow.add(dLbl, BorderLayout.EAST);
        bottomArea.add(verRow);

        sidebar.add(bottomArea, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String text, String iconType, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, isActive ? Font.BOLD : Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setIcon(new NavIcon(iconType, isActive ? Color.WHITE : COLOR_TEXT_MUTED, 18));
        btn.setIconTextGap(12);

        if (isActive) {
            btn.setBackground(COLOR_PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        } else {
            btn.setBackground(COLOR_SURFACE);
            btn.setForeground(COLOR_TEXT_MUTED);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(COLOR_SURFACE_CONTAINER);
                    btn.setForeground(COLOR_TEXT_ON_SURFACE);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(COLOR_SURFACE);
                    btn.setForeground(COLOR_TEXT_MUTED);
                }
            });
        }
        return btn;
    }

    // =========================================================================
    // 2. TOP HEADER COMPONENT
    // =========================================================================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setPreferredSize(new Dimension(0, 64));
        header.setBackground(COLOR_SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_SURFACE_HIGH),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Location selector (West)
        JButton locBtn = new JButton();
        locBtn.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        locBtn.setContentAreaFilled(false);
        locBtn.setBorderPainted(false);
        locBtn.setFocusPainted(false);
        locBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel locPin = new JLabel(new PinIcon(COLOR_PRIMARY, 16));
        locationLabel = new JLabel("Indiranagar 100ft Rd, Bengaluru");
        locationLabel.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        locationLabel.setForeground(COLOR_TEXT_ON_SURFACE);
        JLabel locDown = new JLabel("▾");
        locDown.setForeground(COLOR_TEXT_MUTED);

        locBtn.add(locPin);
        locBtn.add(locationLabel);
        locBtn.add(locDown);
        locBtn.addActionListener(e -> {
            String newLoc = JOptionPane.showInputDialog(this,
                    "Enter your delivery address / area in Bengaluru:",
                    locationLabel.getText());
            if (newLoc != null && !newLoc.trim().isEmpty()) {
                locationLabel.setText(newLoc.trim());
            }
        });
        header.add(locBtn, BorderLayout.WEST);

        // Search Bar (Center)
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(COLOR_SURFACE_LOW);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 8, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 10)
        ));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        searchField = new JTextField();
        searchField.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        searchField.setBackground(COLOR_SURFACE_LOW);
        searchField.setBorder(null);
        searchField.setForeground(COLOR_TEXT_ON_SURFACE);
        searchField.putClientProperty("JTextField.placeholderText",
                "Search dishes, cuisines, meal plans, or vendors...");

        JLabel kbdHint = new JLabel(" ⌘K ");
        kbdHint.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        kbdHint.setForeground(COLOR_TEXT_MUTED);
        kbdHint.setOpaque(true);
        kbdHint.setBackground(COLOR_SURFACE_HIGH);
        kbdHint.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(kbdHint, BorderLayout.EAST);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { updateSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { updateSearch(); }
            private void updateSearch() {
                searchQuery = searchField.getText().trim().toLowerCase();
                filterDishes();
            }
        });

        // Center search in constrained width
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(COLOR_SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.ipadx = 40;
        centerWrap.add(searchPanel, gbc);
        header.add(centerWrap, BorderLayout.CENTER);

        // Right side: Notification, Cart, Profile
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setBackground(COLOR_SURFACE);

        // Notifications
        JButton notifBtn = new JButton("🔔");
        notifBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 15));
        notifBtn.setContentAreaFilled(false);
        notifBtn.setBorderPainted(false);
        notifBtn.setFocusPainted(false);
        notifBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        notifBtn.setToolTipText("Notifications");
        notifBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Notifications:\n• Your Lunch bowl is being packed at The Green Kitchen!\n• Today's dinner cutoff is 6:00 PM.",
                "Notifications", JOptionPane.INFORMATION_MESSAGE));

        // Cart button
        cartBtn = new JButton();
        styleCartButton();
        cartBtn.addActionListener(e -> showCartSummary());

        // Profile avatar
        JButton profileAvatarBtn = createHeaderProfileButton();

        rightActions.add(notifBtn);
        rightActions.add(cartBtn);
        rightActions.add(profileAvatarBtn);
        header.add(rightActions, BorderLayout.EAST);

        return header;
    }

    private void styleCartButton() {
        int count = getCartTotalCount();
        double total = getCartTotalPrice();
        cartBtn.setText(String.format(" 🛍 Cart (%d) • ₹%.0f", count, total));
        cartBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        cartBtn.setBackground(COLOR_PRIMARY_FIXED);
        cartBtn.setForeground(COLOR_ON_PRIMARY_FIXED);
        cartBtn.setFocusPainted(false);
        cartBtn.setBorderPainted(false);
        cartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cartBtn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
    }

    private JButton createHeaderProfileButton() {
        JButton pBtn = new JButton();
        pBtn.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
        pBtn.setContentAreaFilled(false);
        pBtn.setBorderPainted(false);
        pBtn.setFocusPainted(false);
        pBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel avatar = new JLabel(new AvatarIcon(customerName, 30));
        JLabel arrow = new JLabel("▾");
        arrow.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        arrow.setForeground(COLOR_TEXT_MUTED);

        pBtn.add(avatar);
        pBtn.add(arrow);
        pBtn.addActionListener(e -> {
            new ProfilePage(customerName).setVisible(true);
            dispose();
        });
        return pBtn;
    }

    // =========================================================================
    // 3. MAIN SCROLLABLE DASHBOARD CONTENT
    // =========================================================================
    private JPanel createScrollableMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_BG);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 36, 32));

        // 1. Greeting & Live Delivery Status Banner
        content.add(createGreetingSection());
        content.add(Box.createVerticalStrut(24));

        // 2. Curated Meal Plan Hero Banner
        content.add(createHeroBanner());
        content.add(Box.createVerticalStrut(32));

        // 3. Explore by Meal Type (Category Tiles)
        content.add(createMealTypeCategoriesSection());
        content.add(Box.createVerticalStrut(32));

        // 4. Popular Dishes Grid (6 High-Fidelity Cards)
        content.add(createPopularDishesSection());
        content.add(Box.createVerticalStrut(32));

        // 5. Recommended for Your Schedule (Sub-Section)
        content.add(createScheduleRecommendationsSection());
        content.add(Box.createVerticalStrut(32));

        // 6. Featured Neighborhood Kitchens
        content.add(createFeaturedKitchensSection());

        return content;
    }

    // -------------------------------------------------------------------------
    // 3.1 Greeting & Filter Chips Section
    // -------------------------------------------------------------------------
    private JPanel createGreetingSection() {
        JPanel panel = new JPanel(new BorderLayout(16, 12));
        panel.setBackground(COLOR_BG);

        // Left: Greeting titles
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(COLOR_BG);

        // Active Delivery Window Pill
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        pill.setBackground(COLOR_BG);
        pill.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pulseDot = new JLabel(new IndicatorDotIcon(COLOR_SECONDARY, 8));
        JLabel statusText = new JLabel("LUNCH DELIVERY WINDOW ACTIVE");
        statusText.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        statusText.setForeground(COLOR_ON_SEC_CONTAINER);

        JLabel subStatus = new JLabel(" • Estimated arrival by 1:15 PM");
        subStatus.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        subStatus.setForeground(COLOR_OUTLINE);

        JPanel badgeBg = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        badgeBg.setBackground(new Color(0xDC, 0xF5, 0xE7));
        badgeBg.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        badgeBg.add(pulseDot);
        badgeBg.add(statusText);

        pill.add(badgeBg);
        pill.add(subStatus);
        left.add(pill);
        left.add(Box.createVerticalStrut(6));

        JLabel title = new JLabel("Good morning, " + customerName + "! What would you like to eat today?");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);
        left.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Explore fresh neighborhood kitchens or schedule wholesome weekly subscriptions.");
        subtitle.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(subtitle);

        panel.add(left, BorderLayout.WEST);

        // Right: Filter Chips Toolbar
        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        chipsPanel.setBackground(COLOR_BG);

        chipsPanel.add(createFilterChip("Pure Veg", "veg"));
        chipsPanel.add(createFilterChip("High Protein (>25g)", "protein"));
        chipsPanel.add(createFilterChip("Under 30 mins", "speed"));
        chipsPanel.add(createFilterChip("Top Rated (4.5+)", "rating"));

        panel.add(chipsPanel, BorderLayout.EAST);
        return panel;
    }

    private JToggleButton createFilterChip(String label, String filterKey) {
        JToggleButton chip = new JToggleButton(label);
        chip.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        chip.setFocusPainted(false);
        chip.setBorderPainted(false);
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.setBackground(COLOR_SURFACE);
        chip.setForeground(COLOR_TEXT_ON_SURFACE);
        chip.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 16, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        chip.addActionListener(e -> {
            if (chip.isSelected()) {
                activeFilters.add(filterKey);
                chip.setBackground(COLOR_TEXT_ON_SURFACE);
                chip.setForeground(Color.WHITE);
            } else {
                activeFilters.remove(filterKey);
                chip.setBackground(COLOR_SURFACE);
                chip.setForeground(COLOR_TEXT_ON_SURFACE);
            }
            filterDishes();
        });

        return chip;
    }

    // -------------------------------------------------------------------------
    // 3.2 Curated Hero Banner ("Your week, sorted.")
    // -------------------------------------------------------------------------
    private JPanel createHeroBanner() {
        JPanel hero = new GradientRoundedPanel(
                new Color(0xFF, 0xEE, 0xEB),
                new Color(0xEA, 0xF5, 0xEC),
                16
        );
        hero.setLayout(new BorderLayout(24, 16));
        hero.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 16, 1),
                BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));

        // Left text & actions
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Badge
        JLabel badge = new JLabel(" ✓ Flexible Delivery • Pause Anytime • ₹0 Delivery Fee ");
        badge.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        badge.setForeground(COLOR_ON_PRIMARY_FIXED);
        badge.setOpaque(true);
        badge.setBackground(COLOR_PRIMARY_FIXED);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(badge);
        left.add(Box.createVerticalStrut(12));

        JLabel title = new JLabel("Your week, sorted.");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 28));
        title.setForeground(COLOR_TEXT_ON_SURFACE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);
        left.add(Box.createVerticalStrut(8));

        JLabel desc = new JLabel("<html>Plan your meals ahead and make everyday eating effortless with nutritionist-approved<br>weekly packages starting from only <font color='#ab2e16'><b>₹120/meal</b></font>.</html>");
        desc.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        desc.setForeground(COLOR_TEXT_MUTED);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(desc);
        left.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton explorePlansBtn = new JButton("Explore Meal Plans  →");
        explorePlansBtn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        explorePlansBtn.setBackground(COLOR_PRIMARY);
        explorePlansBtn.setForeground(Color.WHITE);
        explorePlansBtn.setFocusPainted(false);
        explorePlansBtn.setBorderPainted(false);
        explorePlansBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        explorePlansBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        explorePlansBtn.addActionListener(e -> {
            new MealPlansPage(customerName).setVisible(true);
            dispose();
        });

        JButton customPlanBtn = new JButton("⚙ Build Custom Plan");
        customPlanBtn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        customPlanBtn.setBackground(COLOR_SURFACE);
        customPlanBtn.setForeground(COLOR_TEXT_ON_SURFACE);
        customPlanBtn.setFocusPainted(false);
        customPlanBtn.setBorderPainted(false);
        customPlanBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        customPlanBtn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 8, 1),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        customPlanBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Custom Meal Builder:\nSelect daily calorie requirements, macro targets, and preferred delivery windows.\n(Module connected to Person 3 customization flow)",
                "Build Custom Plan", JOptionPane.INFORMATION_MESSAGE));

        btnRow.add(explorePlansBtn);
        btnRow.add(customPlanBtn);
        left.add(btnRow);

        hero.add(left, BorderLayout.CENTER);

        // Right side: Stats accent cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        statsPanel.setOpaque(false);

        // Stat Card 1
        JPanel s1 = createStatCard("14,200+", "Active Subscriptions", COLOR_SECONDARY, true);
        // Stat Card 2
        JPanel s2 = createStatCard("4.9 / 5", "Taste Consistency", COLOR_PRIMARY, false);

        statsPanel.add(s1);
        statsPanel.add(s2);
        hero.add(statsPanel, BorderLayout.EAST);

        return hero;
    }

    private JPanel createStatCard(String mainVal, String subVal, Color accent, boolean isProgress) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 12, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setPreferredSize(new Dimension(160, 96));

        JLabel val = new JLabel(mainVal);
        val.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        val.setForeground(accent);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel(subVal);
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        sub.setForeground(COLOR_TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(val);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(8));

        if (isProgress) {
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(78);
            bar.setForeground(COLOR_SECONDARY);
            bar.setBackground(COLOR_SURFACE_HIGH);
            bar.setBorderPainted(false);
            bar.setPreferredSize(new Dimension(120, 6));
            bar.setMaximumSize(new Dimension(120, 6));
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(bar);
        } else {
            JLabel stars = new JLabel("★★★★★");
            stars.setFont(new Font(FONT_NAME, Font.BOLD, 14));
            stars.setForeground(COLOR_AMBER_STAR);
            stars.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(stars);
        }

        return card;
    }

    // -------------------------------------------------------------------------
    // 3.3 Quick Food Categories Tiles (7 Categories)
    // -------------------------------------------------------------------------
    private JPanel createMealTypeCategoriesSection() {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setBackground(COLOR_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG);
        JLabel title = new JLabel("Explore by Meal Type");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        title.setForeground(COLOR_TEXT_ON_SURFACE);

        JLabel sub = new JLabel("Curated categories");
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        sub.setForeground(COLOR_TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(sub, BorderLayout.EAST);
        section.add(header, BorderLayout.NORTH);

        // 7 Category Tiles
        JPanel tilesGrid = new JPanel(new GridLayout(1, 7, 10, 0));
        tilesGrid.setBackground(COLOR_BG);

        tilesGrid.add(createCategoryTile("Breakfast", "18 Kitchens", "🌅", COLOR_PRIMARY_FIXED, COLOR_PRIMARY));
        tilesGrid.add(createCategoryTile("Lunch Bowls", "32 Kitchens", "🥗", COLOR_SECONDARY_FIXED, COLOR_SECONDARY));
        tilesGrid.add(createCategoryTile("Thali & Curries", "26 Kitchens", "🍛", COLOR_TERTIARY_FIXED, COLOR_TERTIARY));
        tilesGrid.add(createCategoryTile("High Protein", "19 Kitchens", "🏋", COLOR_SECONDARY_CONTAINER, COLOR_SECONDARY));
        tilesGrid.add(createCategoryTile("Salads & Wraps", "14 Kitchens", "🌯", COLOR_SURFACE_HIGH, COLOR_PRIMARY));
        tilesGrid.add(createCategoryTile("Healthy Snacks", "22 Kitchens", "🥪", COLOR_PRIMARY_FIXED, COLOR_PRIMARY));
        tilesGrid.add(createCategoryTile("Desserts", "9 Kitchens", "🍨", COLOR_TERTIARY_FIXED, COLOR_TERTIARY));

        section.add(tilesGrid, BorderLayout.CENTER);
        return section;
    }

    private JPanel createCategoryTile(String title, String count, String emoji, Color bgCircle, Color fgColor) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(COLOR_SURFACE);
        tile.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 12, 1),
                BorderFactory.createEmptyBorder(12, 8, 12, 8)
        ));
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Circle with emoji / icon
        JPanel iconCircle = new JPanel(new GridBagLayout());
        iconCircle.setPreferredSize(new Dimension(42, 42));
        iconCircle.setMaximumSize(new Dimension(42, 42));
        iconCircle.setBackground(bgCircle);
        iconCircle.setBorder(new RoundedBorder(bgCircle, 21, 1));
        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 20));
        iconCircle.add(emojiLbl);
        iconCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(title);
        nameLbl.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        nameLbl.setForeground(COLOR_TEXT_ON_SURFACE);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel countLbl = new JLabel(count);
        countLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        countLbl.setForeground(COLOR_OUTLINE);
        countLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        tile.add(iconCircle);
        tile.add(Box.createVerticalStrut(8));
        tile.add(nameLbl);
        tile.add(Box.createVerticalStrut(2));
        tile.add(countLbl);

        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (title.equals(selectedCategory)) {
                    selectedCategory = null;
                    tile.setBackground(COLOR_SURFACE);
                } else {
                    selectedCategory = title;
                    tile.setBackground(COLOR_SURFACE_LOW);
                }
                filterDishes();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!title.equals(selectedCategory)) tile.setBackground(COLOR_SURFACE_LOW);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!title.equals(selectedCategory)) tile.setBackground(COLOR_SURFACE);
            }
        });

        return tile;
    }

    // -------------------------------------------------------------------------
    // 3.4 Popular Dishes Grid (6 High-Fidelity Cards)
    // -------------------------------------------------------------------------
    private JPanel createPopularDishesSection() {
        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setBackground(COLOR_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(COLOR_BG);
        JLabel title = new JLabel("Popular Right Now in Indiranagar");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        title.setForeground(COLOR_TEXT_ON_SURFACE);
        JLabel sub = new JLabel("Top-rated freshly prepared dishes tailored to your preferences");
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        sub.setForeground(COLOR_TEXT_MUTED);
        left.add(title);
        left.add(sub);
        header.add(left, BorderLayout.WEST);

        JButton viewAll = new JButton("View full menu →");
        viewAll.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        viewAll.setForeground(COLOR_PRIMARY);
        viewAll.setContentAreaFilled(false);
        viewAll.setBorderPainted(false);
        viewAll.setFocusPainted(false);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(e -> {
            new MealPlansPage(customerName).setVisible(true);
            dispose();
        });
        header.add(viewAll, BorderLayout.EAST);
        section.add(header, BorderLayout.NORTH);

        // Grid of 6 dish cards (3 cols x 2 rows)
        dishesGridPanel = new JPanel(new GridLayout(0, 3, 16, 16));
        dishesGridPanel.setBackground(COLOR_BG);

        dishCardComponents.clear();
        for (Dish dish : allDishes) {
            DishCardComponent card = new DishCardComponent(dish);
            dishCardComponents.add(card);
            dishesGridPanel.add(card);
        }

        section.add(dishesGridPanel, BorderLayout.CENTER);
        return section;
    }

    private void filterDishes() {
        dishesGridPanel.removeAll();
        for (DishCardComponent comp : dishCardComponents) {
            Dish d = comp.getDish();
            boolean match = true;

            // Search query
            if (!searchQuery.isEmpty()) {
                String full = (d.name + " " + d.kitchen + " " + d.tag + " " + d.category).toLowerCase();
                if (!full.contains(searchQuery)) match = false;
            }

            // Category filter
            if (match && selectedCategory != null) {
                if (!d.category.equalsIgnoreCase(selectedCategory)) match = false;
            }

            // Quick filter chips
            if (match && activeFilters.contains("veg") && !d.isVeg) match = false;
            if (match && activeFilters.contains("protein") && d.proteinGrams < 25) match = false;
            if (match && activeFilters.contains("speed")) {
                int mins = parseMins(d.eta);
                if (mins > 30) match = false;
            }
            if (match && activeFilters.contains("rating") && d.rating < 4.7) match = false;

            if (match) {
                dishesGridPanel.add(comp);
            }
        }
        dishesGridPanel.revalidate();
        dishesGridPanel.repaint();
    }

    private int parseMins(String eta) {
        try {
            String num = eta.replaceAll("[^0-9]", " ");
            String[] parts = num.trim().split("\\s+");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 30;
        }
    }

    // -------------------------------------------------------------------------
    // 3.5 Recommended for Your Schedule (Sub-Section)
    // -------------------------------------------------------------------------
    private JPanel createScheduleRecommendationsSection() {
        JPanel container = new JPanel(new BorderLayout(0, 14));
        container.setBackground(COLOR_SURFACE_LOW);
        container.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 16, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_SURFACE_LOW);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(COLOR_SURFACE_LOW);

        JLabel sparkle = new JLabel("✨");
        sparkle.setFont(new Font(FONT_NAME, Font.PLAIN, 20));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(COLOR_SURFACE_LOW);

        JLabel title = new JLabel("Recommended for Your Schedule");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        title.setForeground(COLOR_TEXT_ON_SURFACE);

        JLabel subtitle = new JLabel("Based on your high-protein target (120g/day) & regular 1:00 PM lunch slot");
        subtitle.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        subtitle.setForeground(COLOR_SECONDARY);

        text.add(title);
        text.add(subtitle);
        left.add(sparkle);
        left.add(text);
        header.add(left, BorderLayout.WEST);

        JLabel refreshHint = new JLabel("Refreshes daily at 10:00 AM");
        refreshHint.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        refreshHint.setForeground(COLOR_OUTLINE);
        header.add(refreshHint, BorderLayout.EAST);

        container.add(header, BorderLayout.NORTH);

        // 3 Cards Row
        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setBackground(COLOR_SURFACE_LOW);

        for (ScheduleItem item : scheduleItems) {
            grid.add(createScheduleCard(item));
        }
        container.add(grid, BorderLayout.CENTER);

        return container;
    }

    private JPanel createScheduleCard(ScheduleItem item) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 12, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // Thumbnail (Left)
        ImagePanel thumb = new ImagePanel(item.imageUrl, 64, 64, 8);
        card.add(thumb, BorderLayout.WEST);

        // Center info
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(COLOR_SURFACE);

        JLabel tag = new JLabel(item.protein + " • ₹" + (int) item.price);
        tag.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        tag.setForeground(COLOR_SECONDARY);

        JLabel name = new JLabel(item.name);
        name.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        name.setForeground(COLOR_TEXT_ON_SURFACE);

        JLabel note = new JLabel(item.note);
        note.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        note.setForeground(COLOR_TEXT_MUTED);

        center.add(tag);
        center.add(Box.createVerticalStrut(2));
        center.add(name);
        center.add(Box.createVerticalStrut(2));
        center.add(note);
        card.add(center, BorderLayout.CENTER);

        // Quick add button (East)
        JButton addBtn = new JButton("+");
        addBtn.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        addBtn.setBackground(COLOR_SURFACE_HIGH);
        addBtn.setForeground(COLOR_TEXT_ON_SURFACE);
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(34, 34));
        addBtn.addActionListener(e -> {
            cartItems.put(item.name, cartItems.getOrDefault(item.name, 0) + 1);
            styleCartButton();
            JOptionPane.showMessageDialog(this, "Added \"" + item.name + "\" to your cart!");
        });
        card.add(addBtn, BorderLayout.EAST);

        return card;
    }

    // -------------------------------------------------------------------------
    // 3.6 Featured Neighborhood Kitchens (3 Vendor Cards)
    // -------------------------------------------------------------------------
    private JPanel createFeaturedKitchensSection() {
        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setBackground(COLOR_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(COLOR_BG);
        JLabel title = new JLabel("Featured Neighborhood Kitchens");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        title.setForeground(COLOR_TEXT_ON_SURFACE);
        JLabel sub = new JLabel("Clean, verified, and subscription-ready home cooks and hygienic cloud kitchens");
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        sub.setForeground(COLOR_TEXT_MUTED);
        left.add(title);
        left.add(sub);
        header.add(left, BorderLayout.WEST);

        JButton viewAll = new JButton("View all 48 kitchens →");
        viewAll.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        viewAll.setForeground(COLOR_PRIMARY);
        viewAll.setContentAreaFilled(false);
        viewAll.setBorderPainted(false);
        viewAll.setFocusPainted(false);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(e -> {
            new MealPlansPage(customerName).setVisible(true);
            dispose();
        });
        header.add(viewAll, BorderLayout.EAST);
        section.add(header, BorderLayout.NORTH);

        // 3 Kitchen Cards
        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setBackground(COLOR_BG);

        kitchenCardComponents.clear();
        for (Kitchen kitchen : allKitchens) {
            KitchenCardComponent kc = new KitchenCardComponent(kitchen);
            kitchenCardComponents.add(kc);
            grid.add(kc);
        }
        section.add(grid, BorderLayout.CENTER);

        return section;
    }

    // =========================================================================
    // CART & DIALOG BEHAVIORS
    // =========================================================================
    private int getCartTotalCount() {
        int c = 0;
        for (int q : cartItems.values()) c += q;
        return c;
    }

    private double getCartTotalPrice() {
        double total = 0;
        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            double price = getPriceForName(entry.getKey());
            total += price * entry.getValue();
        }
        return total;
    }

    private double getPriceForName(String name) {
        for (Dish d : allDishes) {
            if (d.name.equals(name)) return d.price;
        }
        for (ScheduleItem s : scheduleItems) {
            if (s.name.equals(name)) return s.price;
        }
        return 180.0;
    }

    private void showCartSummary() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is currently empty.\nExplore popular dishes or meal plans to add items!", "Cart", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("<html><body style='font-family:sans-serif; width:340px;'>");
        sb.append("<h2 style='color:#ab2e16;'>Your MealFlow Cart</h2><hr>");
        sb.append("<table width='100%' cellpadding='4'>");

        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            double price = getPriceForName(entry.getKey());
            sb.append(String.format("<tr><td><b>%s</b><br><small>Qty: %d</small></td><td align='right'>₹%.0f</td></tr>",
                    entry.getKey(), entry.getValue(), price * entry.getValue()));
        }

        double total = getCartTotalPrice();
        sb.append("</table><hr>");
        sb.append(String.format("<p><b>Delivery:</b> FREE<br><b>Grand Total:</b> <font size='+1' color='#006c4a'><b>₹%.2f</b></font></p>", total));
        sb.append("<p>Proceed to payment or continue shopping?</p></body></html>");

        int choice = JOptionPane.showOptionDialog(
                this,
                new JLabel(sb.toString()),
                "Cart (" + getCartTotalCount() + " items)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"Proceed to Payment", "Keep Browsing"},
                "Proceed to Payment"
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Open Person 4's PaymentPage!
            new PaymentPage(total).setVisible(true);
            dispose();
        }
    }

    private void showWeeklyScheduleDialog() {
        String msg = "<html><body style='width:360px; font-family:sans-serif;'>"
                + "<h2 style='color:#006c4a;'>Weekly Delivery Schedule</h2>"
                + "<p><b>Active Plan:</b> Balanced Week (7 Days • 3 Meals/Day)</p>"
                + "<ul>"
                + "<li><b>Monday - Friday:</b> Lunch at 1:00 PM • Dinner at 8:00 PM</li>"
                + "<li><b>Saturday - Sunday:</b> Brunch at 11:30 AM • Chef's DIY Kit at 7:00 PM</li>"
                + "</ul>"
                + "<p style='color:#59413c;'><small>You can pause or skip upcoming days up to 4 hours before the prep window.</small></p>"
                + "</body></html>";
        JOptionPane.showMessageDialog(this, new JLabel(msg), "Weekly Schedule", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSubscriptionsDialog() {
        String msg = "<html><body style='width:360px; font-family:sans-serif;'>"
                + "<h2 style='color:#ab2e16;'>My Subscriptions</h2>"
                + "<p><b>Plan:</b> Balanced Week Cooked Subscription<br>"
                + "<b>Vendor:</b> The Green Kitchen & Annapurna Tiffins<br>"
                + "<b>Valid Till:</b> Sunday, 31st Oct<br>"
                + "<b>Auto-Renewal:</b> Enabled (UPI Mandate)</p>"
                + "<p>Status: <font color='#006c4a'><b>ACTIVE</b></font></p>"
                + "</body></html>";
        JOptionPane.showMessageDialog(this, new JLabel(msg), "My Subscriptions", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // INNER CLASSES: CARDS & WIDGETS
    // =========================================================================

    /**
     * DishCardComponent - Modern card with culinary preview, veg/non-veg tag, ETA, rating, macros, and interactive add/quantity button.
     */
    private class DishCardComponent extends JPanel {
        private final Dish dish;
        private boolean isFavorite = false;
        private JPanel actionContainer;

        public DishCardComponent(Dish dish) {
            this.dish = dish;
            setLayout(new BorderLayout());
            setBackground(COLOR_SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(COLOR_SURFACE_HIGH, 16, 1),
                    BorderFactory.createEmptyBorder(0, 0, 14, 0)
            ));

            // 1. Image Banner with Overlaid Badges
            JPanel banner = createBanner();
            add(banner, BorderLayout.NORTH);

            // 2. Info Body
            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBackground(COLOR_SURFACE);
            body.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

            // Kitchen + Rating
            JPanel row1 = new JPanel(new BorderLayout());
            row1.setBackground(COLOR_SURFACE);
            JLabel kitchenLbl = new JLabel(dish.kitchen);
            kitchenLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            kitchenLbl.setForeground(COLOR_OUTLINE);

            JLabel ratingLbl = new JLabel("★ " + dish.rating + " (" + dish.reviewCount + ")");
            ratingLbl.setFont(new Font(FONT_NAME, Font.BOLD, 11));
            ratingLbl.setForeground(COLOR_TEXT_ON_SURFACE);
            row1.add(kitchenLbl, BorderLayout.WEST);
            row1.add(ratingLbl, BorderLayout.EAST);
            body.add(row1);
            body.add(Box.createVerticalStrut(4));

            // Title
            JLabel titleLbl = new JLabel(dish.name);
            titleLbl.setFont(new Font(FONT_NAME, Font.BOLD, 14));
            titleLbl.setForeground(COLOR_TEXT_ON_SURFACE);
            body.add(titleLbl);
            body.add(Box.createVerticalStrut(6));

            // Macros strip
            JPanel macrosRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            macrosRow.setBackground(COLOR_SURFACE);

            JLabel proteinBadge = new JLabel(" " + dish.proteinGrams + "g Protein ");
            proteinBadge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
            proteinBadge.setForeground(COLOR_ON_SEC_CONTAINER);
            proteinBadge.setOpaque(true);
            proteinBadge.setBackground(COLOR_SECONDARY_CONTAINER);
            proteinBadge.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

            JLabel calLbl = new JLabel(dish.calories + " kcal • " + dish.tag);
            calLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            calLbl.setForeground(COLOR_TEXT_MUTED);

            macrosRow.add(proteinBadge);
            macrosRow.add(calLbl);
            body.add(macrosRow);
            add(body, BorderLayout.CENTER);

            // 3. Bottom Row: Price & Add / Quantity
            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(COLOR_SURFACE);
            footer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_SURFACE_HIGH),
                    BorderFactory.createEmptyBorder(10, 16, 0, 16)
            ));

            JPanel priceBox = new JPanel();
            priceBox.setLayout(new BoxLayout(priceBox, BoxLayout.Y_AXIS));
            priceBox.setBackground(COLOR_SURFACE);
            JLabel priceSub = new JLabel("Price");
            priceSub.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
            priceSub.setForeground(COLOR_OUTLINE);
            JLabel priceVal = new JLabel("₹" + (int) dish.price);
            priceVal.setFont(new Font(FONT_NAME, Font.BOLD, 16));
            priceVal.setForeground(COLOR_TEXT_ON_SURFACE);
            priceBox.add(priceSub);
            priceBox.add(priceVal);
            footer.add(priceBox, BorderLayout.WEST);

            actionContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            actionContainer.setBackground(COLOR_SURFACE);
            updateActionButtonState();
            footer.add(actionContainer, BorderLayout.EAST);

            add(footer, BorderLayout.SOUTH);
        }

        private JPanel createBanner() {
            JPanel banner = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    BufferedImage img = imageCache.get(dish.imageUrl);
                    if (img != null) {
                        g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                    } else {
                        // Soft modern gradient placeholder while loading
                        GradientPaint gp = new GradientPaint(0, 0, new Color(0xE7, 0xF2, 0xEA), getWidth(), getHeight(), new Color(0xDC, 0xE9, 0xDF));
                        g2.setPaint(gp);
                        g2.fillRect(0, 0, getWidth(), getHeight());

                        g2.setColor(COLOR_TEXT_MUTED);
                        g2.setFont(new Font(FONT_NAME, Font.BOLD, 14));
                        g2.drawString(dish.category, 16, getHeight() / 2);
                    }
                    g2.dispose();
                }
            };
            banner.setPreferredSize(new Dimension(0, 150));
            banner.setLayout(new BorderLayout());

            // Trigger background async image load
            triggerImageLoad(dish.imageUrl, banner);

            // Top overlay: Veg Badge (West) and Heart Favorite (East)
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);
            topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

            // Veg Badge
            JPanel vegBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            vegBadge.setBackground(new Color(255, 255, 255, 230));
            vegBadge.setBorder(new RoundedBorder(COLOR_SURFACE_HIGH, 6, 1));
            JLabel vegIcon = new JLabel(new VegIndicatorIcon(dish.isVeg, 12));
            JLabel vegText = new JLabel(dish.isVegan ? "Vegan" : (dish.isVeg ? "Veg" : "Non-Veg"));
            vegText.setFont(new Font(FONT_NAME, Font.BOLD, 10));
            vegText.setForeground(dish.isVeg ? COLOR_SECONDARY : COLOR_PRIMARY);
            vegBadge.add(vegIcon);
            vegBadge.add(vegText);
            topBar.add(vegBadge, BorderLayout.WEST);

            // Heart / Favorite
            JButton heartBtn = new JButton("♡");
            heartBtn.setFont(new Font(FONT_NAME, Font.BOLD, 16));
            heartBtn.setForeground(COLOR_TEXT_MUTED);
            heartBtn.setBackground(new Color(255, 255, 255, 230));
            heartBtn.setFocusPainted(false);
            heartBtn.setBorderPainted(false);
            heartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            heartBtn.setPreferredSize(new Dimension(30, 30));
            heartBtn.addActionListener(e -> {
                isFavorite = !isFavorite;
                heartBtn.setText(isFavorite ? "♥" : "♡");
                heartBtn.setForeground(isFavorite ? COLOR_PRIMARY : COLOR_TEXT_MUTED);
            });
            topBar.add(heartBtn, BorderLayout.EAST);
            banner.add(topBar, BorderLayout.NORTH);

            // Bottom overlay: ETA badge
            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            bottomBar.setOpaque(false);
            bottomBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));

            JLabel etaBadge = new JLabel(" ⏱ " + dish.eta + " ");
            etaBadge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
            etaBadge.setForeground(Color.WHITE);
            etaBadge.setOpaque(true);
            etaBadge.setBackground(new Color(0x2A, 0x32, 0x2D, 210));
            etaBadge.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            bottomBar.add(etaBadge);
            banner.add(bottomBar, BorderLayout.SOUTH);

            return banner;
        }

        private void updateActionButtonState() {
            actionContainer.removeAll();
            int qty = cartItems.getOrDefault(dish.name, 0);

            if (qty <= 0) {
                JButton addBtn = new JButton("+ Add");
                addBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
                addBtn.setBackground(COLOR_PRIMARY);
                addBtn.setForeground(Color.WHITE);
                addBtn.setFocusPainted(false);
                addBtn.setBorderPainted(false);
                addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
                addBtn.addActionListener(e -> {
                    cartItems.put(dish.name, 1);
                    updateActionButtonState();
                    styleCartButton();
                });
                actionContainer.add(addBtn);
            } else {
                // Stepper [- N +]
                JPanel stepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
                stepper.setBackground(COLOR_SURFACE_LOW);
                stepper.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(COLOR_SURFACE_HIGH, 6, 1),
                        BorderFactory.createEmptyBorder(2, 4, 2, 4)
                ));

                JButton minusBtn = new JButton("−");
                minusBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
                minusBtn.setBackground(COLOR_SURFACE);
                minusBtn.setForeground(COLOR_TEXT_ON_SURFACE);
                minusBtn.setFocusPainted(false);
                minusBtn.setBorderPainted(false);
                minusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                minusBtn.setPreferredSize(new Dimension(24, 24));
                minusBtn.addActionListener(e -> {
                    int cur = cartItems.getOrDefault(dish.name, 0);
                    if (cur <= 1) cartItems.remove(dish.name);
                    else cartItems.put(dish.name, cur - 1);
                    updateActionButtonState();
                    styleCartButton();
                });

                JLabel countLbl = new JLabel(String.valueOf(qty));
                countLbl.setFont(new Font(FONT_NAME, Font.BOLD, 12));
                countLbl.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                JButton plusBtn = new JButton("+");
                plusBtn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
                plusBtn.setBackground(COLOR_PRIMARY);
                plusBtn.setForeground(Color.WHITE);
                plusBtn.setFocusPainted(false);
                plusBtn.setBorderPainted(false);
                plusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                plusBtn.setPreferredSize(new Dimension(24, 24));
                plusBtn.addActionListener(e -> {
                    cartItems.put(dish.name, cartItems.getOrDefault(dish.name, 0) + 1);
                    updateActionButtonState();
                    styleCartButton();
                });

                stepper.add(minusBtn);
                stepper.add(countLbl);
                stepper.add(plusBtn);
                actionContainer.add(stepper);
            }

            actionContainer.revalidate();
            actionContainer.repaint();
        }

        public Dish getDish() { return dish; }
    }

    /**
     * KitchenCardComponent - Neighborhood cloud kitchen card with culinary photo, badges, rating, and subscription availability.
     */
    private class KitchenCardComponent extends JPanel {
        private final Kitchen kitchen;

        public KitchenCardComponent(Kitchen kitchen) {
            this.kitchen = kitchen;
            setLayout(new BorderLayout(0, 12));
            setBackground(COLOR_SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(COLOR_SURFACE_HIGH, 16, 1),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)
            ));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Thumbnail Banner
            JPanel banner = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    BufferedImage img = imageCache.get(kitchen.imageUrl);
                    if (img != null) {
                        g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                    } else {
                        GradientPaint gp = new GradientPaint(0, 0, new Color(0xEE, 0xF5, 0xF0), getWidth(), getHeight(), new Color(0xDC, 0xE8, 0xDF));
                        g2.setPaint(gp);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }
                    g2.dispose();
                }
            };
            banner.setPreferredSize(new Dimension(0, 120));
            banner.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
            triggerImageLoad(kitchen.imageUrl, banner);

            JLabel badge = new JLabel(" " + kitchen.badge + " ");
            badge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
            badge.setForeground(Color.WHITE);
            badge.setOpaque(true);
            badge.setBackground(kitchen.accentColor);
            badge.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            banner.add(badge);
            add(banner, BorderLayout.NORTH);

            // Details
            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBackground(COLOR_SURFACE);

            JPanel r1 = new JPanel(new BorderLayout());
            r1.setBackground(COLOR_SURFACE);
            JLabel name = new JLabel(kitchen.name);
            name.setFont(new Font(FONT_NAME, Font.BOLD, 14));
            name.setForeground(COLOR_TEXT_ON_SURFACE);

            JLabel rating = new JLabel("★ " + kitchen.rating + " (" + (kitchen.reviewCount > 999 ? (kitchen.reviewCount / 1000.0) + "k" : kitchen.reviewCount) + ")");
            rating.setFont(new Font(FONT_NAME, Font.BOLD, 11));
            rating.setForeground(COLOR_TEXT_ON_SURFACE);
            r1.add(name, BorderLayout.WEST);
            r1.add(rating, BorderLayout.EAST);
            body.add(r1);
            body.add(Box.createVerticalStrut(4));

            JLabel desc = new JLabel(kitchen.cuisine);
            desc.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            desc.setForeground(COLOR_TEXT_MUTED);
            body.add(desc);
            body.add(Box.createVerticalStrut(8));

            JLabel meta = new JLabel("⏱ " + kitchen.eta + " • ₹" + (int) kitchen.priceForOne + " for one • " + kitchen.highlight);
            meta.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            meta.setForeground(COLOR_SECONDARY);
            body.add(meta);

            add(body, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JOptionPane.showMessageDialog(ExplorePage.this,
                            kitchen.name + "\n" + kitchen.cuisine + "\n\nAvailable on MealFlow Daily Tiffin & Meal Kit plans.",
                            "Kitchen Details", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
    }

    // =========================================================================
    // ASYNC IMAGE LOADER & CACHING
    // =========================================================================
    private static void triggerImageLoad(String urlString, JComponent repaintTarget) {
        if (urlString == null || urlString.isEmpty() || imageCache.containsKey(urlString)) return;

        imagePool.submit(() -> {
            try {
                URL url = URI.create(urlString).toURL();
                BufferedImage img = ImageIO.read(url);
                if (img != null) {
                    imageCache.put(urlString, img);
                    SwingUtilities.invokeLater(repaintTarget::repaint);
                }
            } catch (Exception ignored) {
                // Silently fallback to clean styled vector graphics
            }
        });
    }

    private static class ImagePanel extends JPanel {
        private final String url;
        private final int width;
        private final int height;
        private final int arc;

        public ImagePanel(String url, int width, int height, int arc) {
            this.url = url;
            this.width = width;
            this.height = height;
            this.arc = arc;
            setPreferredSize(new Dimension(width, height));
            setOpaque(false);
            triggerImageLoad(url, this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape clip = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
            g2.setClip(clip);

            BufferedImage img = imageCache.get(url);
            if (img != null) {
                g2.drawImage(img, 0, 0, width, height, null);
            } else {
                g2.setColor(COLOR_SURFACE_HIGH);
                g2.fillRect(0, 0, width, height);
                g2.setColor(COLOR_TEXT_MUTED);
                g2.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
                g2.drawString("🍲", width / 4, (int) (height * 0.65));
            }
            g2.dispose();
        }
    }

    // =========================================================================
    // GRAPHICAL HELPERS & VECTOR ICONS
    // =========================================================================
    private static class GradientRoundedPanel extends JPanel {
        private final Color c1;
        private final Color c2;
        private final int arc;

        public GradientRoundedPanel(Color c1, Color c2, int arc) {
            this.c1 = c1;
            this.c2 = c2;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
        }
    }

    private static class RoundedBorder implements Border {
        private final Color color;
        private final int arc;
        private final int thickness;

        public RoundedBorder(Color color, int arc, int thickness) {
            this.color = color;
            this.arc = arc;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 4, 4, 4); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    private static class LeafLogoIcon implements Icon {
        private final int size;
        public LeafLogoIcon(int size) { this.size = size; }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_SECONDARY);
            g2.fillRoundRect(x, y, size, size, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(FONT_NAME, Font.BOLD, 16));
            g2.drawString("M", x + (size / 4), y + (int) (size * 0.75));
            g2.dispose();
        }
    }

    private static class NavIcon implements Icon {
        private final String type;
        private final Color color;
        private final int size;

        public NavIcon(String type, Color color, int size) {
            this.type = type;
            this.color = color;
            this.size = size;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setFont(new Font(FONT_NAME, Font.PLAIN, size - 2));

            String symbol;
            switch (type) {
                case "explore": symbol = "🧭"; break;
                case "restaurant_menu": symbol = "🍽"; break;
                case "calendar_today": symbol = "📅"; break;
                case "receipt_long": symbol = "🧾"; break;
                case "autorenew": symbol = "🔄"; break;
                case "manage_accounts": symbol = "⚙"; break;
                default: symbol = "•"; break;
            }
            g2.drawString(symbol, x, y + size - 2);
            g2.dispose();
        }
    }

    private static class VegIndicatorIcon implements Icon {
        private final boolean isVeg;
        private final int size;

        public VegIndicatorIcon(boolean isVeg, int size) {
            this.isVeg = isVeg;
            this.size = size;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color border = isVeg ? COLOR_SECONDARY : COLOR_PRIMARY;
            g2.setColor(border);
            g2.drawRect(x, y, size - 1, size - 1);
            g2.fillOval(x + 2, y + 2, size - 4, size - 4);
            g2.dispose();
        }
    }

    private static class IndicatorDotIcon implements Icon {
        private final Color color;
        private final int diameter;
        public IndicatorDotIcon(Color color, int diameter) {
            this.color = color;
            this.diameter = diameter;
        }
        @Override public int getIconWidth() { return diameter; }
        @Override public int getIconHeight() { return diameter; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, diameter, diameter);
            g2.dispose();
        }
    }

    private static class PinIcon implements Icon {
        private final Color color;
        private final int size;
        public PinIcon(Color color, int size) { this.color = color; this.size = size; }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setFont(new Font(FONT_NAME, Font.PLAIN, size));
            g2.drawString("📍", x, y + size - 2);
            g2.dispose();
        }
    }

    private static class AvatarIcon implements Icon {
        private final String name;
        private final int size;

        public AvatarIcon(String name, int size) {
            this.name = name;
            this.size = size;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_PRIMARY);
            g2.fillOval(x, y, size, size);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font(FONT_NAME, Font.BOLD, (int) (size * 0.45)));
            String initial = (name != null && !name.trim().isEmpty())
                    ? name.trim().substring(0, 1).toUpperCase()
                    : "U";
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(initial)) / 2;
            int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(initial, tx, ty);
            g2.dispose();
        }
    }

    // =========================================================================
    // DATA POJOs
    // =========================================================================
    public static class Dish {
        public final String name;
        public final String kitchen;
        public final double rating;
        public final int reviewCount;
        public final boolean isVeg;
        public final boolean isVegan;
        public final String eta;
        public final int proteinGrams;
        public final int calories;
        public final String tag;
        public final double price;
        public final String imageUrl;
        public final String category;

        public Dish(String name, String kitchen, double rating, int reviewCount,
                    boolean isVeg, boolean isVegan, String eta, int proteinGrams,
                    int calories, String tag, double price, String imageUrl, String category) {
            this.name = name;
            this.kitchen = kitchen;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.isVeg = isVeg;
            this.isVegan = isVegan;
            this.eta = eta;
            this.proteinGrams = proteinGrams;
            this.calories = calories;
            this.tag = tag;
            this.price = price;
            this.imageUrl = imageUrl;
            this.category = category;
        }
    }

    public static class Kitchen {
        public final String name;
        public final String badge;
        public final double rating;
        public final int reviewCount;
        public final String cuisine;
        public final String eta;
        public final double priceForOne;
        public final String highlight;
        public final Color accentColor;
        public final String imageUrl;

        public Kitchen(String name, String badge, double rating, int reviewCount,
                       String cuisine, String eta, double priceForOne, String highlight,
                       Color accentColor, String imageUrl) {
            this.name = name;
            this.badge = badge;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.cuisine = cuisine;
            this.eta = eta;
            this.priceForOne = priceForOne;
            this.highlight = highlight;
            this.accentColor = accentColor;
            this.imageUrl = imageUrl;
        }
    }

    public static class ScheduleItem {
        public final String name;
        public final String protein;
        public final double price;
        public final String note;
        public final String imageUrl;

        public ScheduleItem(String name, String protein, double price, String note, String imageUrl) {
            this.name = name;
            this.protein = protein;
            this.price = price;
            this.note = note;
            this.imageUrl = imageUrl;
        }
    }

    private static String getSafeFontFamily() {
        String[] preferred = {"Plus Jakarta Sans", "Inter", "Segoe UI", "San Francisco", "Helvetica Neue", "Arial"};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Set<String> set = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));
        for (String f : preferred) {
            if (set.contains(f)) return f;
        }
        return Font.SANS_SERIF;
    }

    // =========================================================================
    // MAIN METHOD
    // =========================================================================
    public static void main(String[] args) {
        UIStyle.setup();
        SwingUtilities.invokeLater(() -> new ExplorePage("Rahul").setVisible(true));
    }
}
