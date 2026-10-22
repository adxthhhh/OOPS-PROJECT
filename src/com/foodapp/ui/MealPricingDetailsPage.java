package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.Customer;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * MealPricingDetailsPage - High-fidelity Java Swing equivalent of the Meal Pricing & Details HTML/CSS.
 * Features a modern two-column desktop modal layout with:
 * - 4:3 food visual with bestseller, veg, and plan badges
 * - Donut macro distribution chart (Protein 28%, Carbs 48%, Fats 24%)
 * - 4 Key macro meters and micronutrient tags
 * - Interactive base choice (Radio), protein boosts, and sides/refreshments (Checkboxes)
 * - Live dynamic unit and grand total price calculations
 * - Quantity stepper and animated feedback toast
 * - Direct transition to checkout / PaymentPage
 */
public class MealPricingDetailsPage extends JFrame {

    // --- Color Palette (Tailwind Design System Tokens) ---
    public static final Color COLOR_PRIMARY = new Color(0xAB, 0x2E, 0x16);
    public static final Color COLOR_PRIMARY_CONTAINER = new Color(0xCE, 0x46, 0x2C);
    public static final Color COLOR_PRIMARY_FIXED = new Color(0xFF, 0xDA, 0xD3);
    public static final Color COLOR_ON_PRIMARY_FIXED = new Color(0x3E, 0x04, 0x00);
    public static final Color COLOR_SECONDARY = new Color(0x00, 0x6C, 0x4A);
    public static final Color COLOR_SECONDARY_CONTAINER = new Color(0x82, 0xF5, 0xC1);
    public static final Color COLOR_SECONDARY_FIXED = new Color(0x85, 0xF8, 0xC4);
    public static final Color COLOR_ON_SECONDARY_FIXED = new Color(0x00, 0x21, 0x14);
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

    // --- State Variables ---
    private final String customerName;
    private final int baseDishPrice = 240;
    private int quantity = 1;
    private boolean isFavorite = false;

    // Customization selections
    private final JRadioButton baseBrownRice = new JRadioButton("Brown Basmati Rice (Standard)");
    private final JRadioButton baseQuinoa = new JRadioButton("Quinoa Pilaf (+₹40)");
    private final JRadioButton baseCauliflower = new JRadioButton("Cauliflower Keto Rice (+₹35)");

    private final JCheckBox extraPaneer = new JCheckBox("Extra Charred Paneer (+80g) (+₹60)");
    private final JCheckBox chickpeas = new JCheckBox("Boiled Organic Chickpeas (+₹30)");

    private final JCheckBox extraMintChutney = new JCheckBox("Extra Mint Chutney (50ml) (+₹15)");
    private final JCheckBox valenciaJuice = new JCheckBox("Valencia Orange Juice 200ml (+₹85)");

    private final JTextField kitchenNotes = new JTextField();

    // Live display labels
    private JLabel qtyDisplay;
    private JButton qtyMinusBtn;
    private JLabel unitPriceLabel;
    private JButton addToCartBtn;
    private JButton favBtn;

    // Toast overlay
    private JPanel toastPanel;
    private JLabel toastLabel;
    private Timer toastTimer;

    public MealPricingDetailsPage() {
        this("Rahul");
    }

    public MealPricingDetailsPage(String customerName) {
        this.customerName = (customerName != null && !customerName.trim().isEmpty()) ? customerName : "Rahul";

        setTitle("MealFlow - Dish Customization & Nutrition Details");
        setSize(1080, 840);
        setMinimumSize(new Dimension(920, 720));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Root container with layered pane for toast overlay
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        // Modal Card Shell
        JPanel modalShell = new JPanel(new BorderLayout());
        modalShell.setBackground(COLOR_SURFACE_LOWEST);
        modalShell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(16, 24, 16, 24),
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 16, 1)
        ));

        // 1. Top Navigation Bar
        modalShell.add(createTopBar(), BorderLayout.NORTH);

        // 2. Scrollable Two-Column Stage
        JScrollPane scrollPane = new JScrollPane(createTwoColumnStage());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(COLOR_SURFACE_LOWEST);
        scrollPane.getViewport().setBackground(COLOR_SURFACE_LOWEST);
        modalShell.add(scrollPane, BorderLayout.CENTER);

        // 3. Sticky Bottom Action Bar
        modalShell.add(createStickyFooter(), BorderLayout.SOUTH);

        // Add modalShell and Toast overlay to frame
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(COLOR_BACKGROUND);
        rootPanel.add(modalShell, BorderLayout.CENTER);

        // Layered wrapper for toast
        JPanel layeredWrapper = new JPanel(null);
        layeredWrapper.setBackground(COLOR_BACKGROUND);

        // Simple toast panel
        toastPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        toastPanel.setBackground(COLOR_INVERSE_SURFACE);
        toastPanel.setBorder(new RoundedBorder(new Color(0x44, 0x50, 0x47), 12, 1));
        toastPanel.setVisible(false);

        toastLabel = new JLabel("Added to cart successfully");
        toastLabel.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        toastLabel.setForeground(Color.WHITE);
        toastPanel.add(toastLabel);

        // Layout with JLayeredPane
        JLayeredPane lp = getLayeredPane();
        rootPanel.setBounds(0, 0, 1080, 800);
        lp.add(rootPanel, JLayeredPane.DEFAULT_LAYER);

        toastPanel.setBounds(380, 720, 320, 42);
        lp.add(toastPanel, JLayeredPane.POPUP_LAYER);

        // Window resize listener to center toast and keep root filled
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                rootPanel.setBounds(0, 0, w, h - 35);
                toastPanel.setBounds((w - 340) / 2, h - 100, 340, 42);
                rootPanel.revalidate();
            }
        });

        calculateTotal();
    }

    // =========================================================================
    // 1. TOP BAR NAVIGATION
    // =========================================================================
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(COLOR_SURFACE_LOWEST);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_SURFACE_HIGH),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        // Breadcrumb
        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        breadcrumb.setOpaque(false);

        JLabel part1 = createClickableLabel("Bowls", COLOR_ON_SURFACE_VARIANT, COLOR_PRIMARY);
        JLabel slash1 = new JLabel("/");
        slash1.setForeground(COLOR_OUTLINE_VARIANT);
        JLabel part2 = createClickableLabel("High-Protein", COLOR_ON_SURFACE_VARIANT, COLOR_PRIMARY);
        JLabel slash2 = new JLabel("/");
        slash2.setForeground(COLOR_OUTLINE_VARIANT);
        JLabel part3 = new JLabel("The Green Kitchen");
        part3.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        part3.setForeground(COLOR_ON_SURFACE);

        breadcrumb.add(part1);
        breadcrumb.add(slash1);
        breadcrumb.add(part2);
        breadcrumb.add(slash2);
        breadcrumb.add(part3);
        bar.add(breadcrumb, BorderLayout.WEST);

        // Action Buttons: Share, Bookmark, Close
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton shareBtn = createCircleIconButton("↗ Share", "Share this dish");
        shareBtn.addActionListener(e -> showToast("Dish link copied to clipboard!"));

        favBtn = createCircleIconButton("♡ Save", "Save to favorites");
        favBtn.addActionListener(e -> toggleFavorite());

        JButton closeBtn = createCircleIconButton("✕", "Close");
        closeBtn.addActionListener(e -> dispose());

        actions.add(shareBtn);
        actions.add(favBtn);
        actions.add(closeBtn);
        bar.add(actions, BorderLayout.EAST);

        return bar;
    }

    // =========================================================================
    // 2. TWO-COLUMN MAIN STAGE
    // =========================================================================
    private JPanel createTwoColumnStage() {
        JPanel stage = new JPanel(new GridBagLayout());
        stage.setBackground(COLOR_SURFACE_LOWEST);
        stage.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;

        // Left Column (Visuals & Nutrition Labelling - ~45% width)
        gbc.gridx = 0;
        gbc.weightx = 0.45;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 16);
        stage.add(createLeftColumn(), gbc);

        // Right Column (Dish Info, Ingredients, Customization Form - ~55% width)
        gbc.gridx = 1;
        gbc.weightx = 0.55;
        gbc.insets = new Insets(0, 16, 0, 0);
        stage.add(createRightColumn(), gbc);

        return stage;
    }

    // -------------------------------------------------------------------------
    // LEFT COLUMN: Visuals & Nutrition Labelling
    // -------------------------------------------------------------------------
    private JPanel createLeftColumn() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(COLOR_SURFACE_LOWEST);

        // 1. Food Visual Container (4:3 aspect panel with badges)
        FoodVisualPanel foodVisual = new FoodVisualPanel();
        foodVisual.setPreferredSize(new Dimension(380, 240));
        foodVisual.setMaximumSize(new Dimension(600, 260));
        foodVisual.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(foodVisual);
        left.add(Box.createVerticalStrut(18));

        // 2. Nutritional Breakdown Card
        JPanel nutritionCard = new JPanel();
        nutritionCard.setLayout(new BoxLayout(nutritionCard, BoxLayout.Y_AXIS));
        nutritionCard.setBackground(COLOR_SURFACE_LOW);
        nutritionCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 12, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        nutritionCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel nTitle = new JLabel("🥗 Nutritional Breakdown");
        nTitle.setFont(new Font(FONT_NAME, Font.BOLD, 15));
        nTitle.setForeground(COLOR_ON_SURFACE);

        JLabel perServingBadge = new JLabel(" Per Serving ");
        perServingBadge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        perServingBadge.setOpaque(true);
        perServingBadge.setBackground(COLOR_SURFACE_HIGHEST);
        perServingBadge.setForeground(COLOR_ON_SURFACE_VARIANT);
        perServingBadge.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

        cardHeader.add(nTitle, BorderLayout.WEST);
        cardHeader.add(perServingBadge, BorderLayout.EAST);
        nutritionCard.add(cardHeader);
        nutritionCard.add(Box.createVerticalStrut(12));

        // Calories Banner + Macro Donut Chart
        JPanel caloriesBanner = new JPanel(new BorderLayout(12, 0));
        caloriesBanner.setBackground(COLOR_SURFACE_LOWEST);
        caloriesBanner.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 8, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JPanel energyText = new JPanel();
        energyText.setLayout(new BoxLayout(energyText, BoxLayout.Y_AXIS));
        energyText.setOpaque(false);

        JLabel energyCaption = new JLabel("TOTAL ENERGY");
        energyCaption.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        energyCaption.setForeground(COLOR_ON_SURFACE_VARIANT);

        JLabel kcalValue = new JLabel("<html><font color='#ab2e16' size='+2'><b>520</b></font> <font color='#59413c' size='-1'>kcal</font></html>");
        kcalValue.setFont(new Font(FONT_NAME, Font.BOLD, 22));

        energyText.add(energyCaption);
        energyText.add(Box.createVerticalStrut(2));
        energyText.add(kcalValue);

        // Donut Macro Graphic Panel (28% Protein, 48% Carbs, 24% Fats)
        MacroDonutPanel donut = new MacroDonutPanel(28, 48, 24);
        donut.setPreferredSize(new Dimension(54, 54));

        caloriesBanner.add(energyText, BorderLayout.WEST);
        caloriesBanner.add(donut, BorderLayout.EAST);
        nutritionCard.add(caloriesBanner);
        nutritionCard.add(Box.createVerticalStrut(12));

        // 4 Key Macros Grid (Protein, Carbs, Fats, Fiber)
        JPanel macroGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        macroGrid.setOpaque(false);
        macroGrid.add(createMacroTile("Protein", "28g", COLOR_SECONDARY, 0.70f));
        macroGrid.add(createMacroTile("Carbs", "48g", COLOR_TERTIARY, 0.60f));
        macroGrid.add(createMacroTile("Good Fats", "14g", COLOR_PRIMARY, 0.35f));
        macroGrid.add(createMacroTile("Fiber", "12g", COLOR_ON_SURFACE, 0.85f));
        nutritionCard.add(macroGrid);
        nutritionCard.add(Box.createVerticalStrut(12));

        // Micronutrients & Dietary Pill Badges
        JPanel microRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        microRow.setOpaque(false);
        microRow.add(createTagPill("Calcium 380mg", COLOR_SURFACE_HIGH, COLOR_ON_SURFACE));
        microRow.add(createTagPill("Iron 3.2mg", COLOR_SURFACE_HIGH, COLOR_ON_SURFACE));
        microRow.add(createTagPill("No Added Sugar", COLOR_SECONDARY_CONTAINER, COLOR_SECONDARY));
        nutritionCard.add(microRow);
        nutritionCard.add(Box.createVerticalStrut(10));

        // Allergens callout
        JPanel allergenBox = new JPanel(new BorderLayout(6, 0));
        allergenBox.setBackground(COLOR_SURFACE_LOWEST);
        allergenBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 6, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JLabel allergenText = new JLabel("<html><b>Allergens:</b> Contains Dairy (Paneer & Curd). Free from Peanuts, Refined Flours, and Trans Fats.</html>");
        allergenText.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        allergenText.setForeground(COLOR_ON_SURFACE_VARIANT);
        allergenBox.add(allergenText, BorderLayout.CENTER);
        nutritionCard.add(allergenBox);
        nutritionCard.add(Box.createVerticalStrut(10));

        // Verification & Audit Seal
        JPanel seal = new JPanel(new BorderLayout(4, 2));
        seal.setBackground(COLOR_SURFACE_LOWEST);
        seal.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 6, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JLabel sealTitle = new JLabel("✓ Verified Kitchen Recipe • MealFlow Labs Certified");
        sealTitle.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        sealTitle.setForeground(COLOR_SECONDARY);
        JLabel sealDesc = new JLabel("<html>Calculated from standardized batch recipes (±5% moisture variance).</html>");
        sealDesc.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        sealDesc.setForeground(COLOR_OUTLINE);
        seal.add(sealTitle, BorderLayout.NORTH);
        seal.add(sealDesc, BorderLayout.CENTER);
        nutritionCard.add(seal);

        left.add(nutritionCard);
        return left;
    }

    // -------------------------------------------------------------------------
    // RIGHT COLUMN: Dish Info, Ingredients, Customization Form
    // -------------------------------------------------------------------------
    private JPanel createRightColumn() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(COLOR_SURFACE_LOWEST);

        // 1. Dish Title & Price Header
        JPanel titleHeader = new JPanel(new BorderLayout());
        titleHeader.setOpaque(false);

        JLabel title = new JLabel("Paneer Tikka Brown Rice Bowl");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        title.setForeground(COLOR_ON_SURFACE);

        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
        pricePanel.setOpaque(false);
        JLabel priceLbl = new JLabel("₹240");
        priceLbl.setFont(new Font(FONT_NAME, Font.BOLD, 22));
        priceLbl.setForeground(COLOR_PRIMARY);
        priceLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel taxLbl = new JLabel("Incl. all taxes");
        taxLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        taxLbl.setForeground(COLOR_OUTLINE);
        taxLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        pricePanel.add(priceLbl);
        pricePanel.add(taxLbl);

        titleHeader.add(title, BorderLayout.CENTER);
        titleHeader.add(pricePanel, BorderLayout.EAST);
        right.add(titleHeader);
        right.add(Box.createVerticalStrut(8));

        // 2. Vendor Credential Ribbon
        JPanel vendorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        vendorRow.setOpaque(false);

        JLabel vendorName = new JLabel("The Green Kitchen (Indiranagar) ↗");
        vendorName.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        vendorName.setForeground(COLOR_ON_SURFACE);

        JLabel ratingBadge = new JLabel(" ★ 4.8 ");
        ratingBadge.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        ratingBadge.setOpaque(true);
        ratingBadge.setBackground(COLOR_SECONDARY_FIXED);
        ratingBadge.setForeground(COLOR_ON_SECONDARY_FIXED);
        ratingBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel prepTime = new JLabel("⏱ 25-30 min prep");
        prepTime.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        prepTime.setForeground(COLOR_OUTLINE);

        vendorRow.add(vendorName);
        vendorRow.add(ratingBadge);
        vendorRow.add(prepTime);
        right.add(vendorRow);
        right.add(Box.createVerticalStrut(10));

        // 3. Serving specification badge
        JPanel servingBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        servingBadge.setBackground(COLOR_SURFACE_CONTAINER);
        servingBadge.setBorder(new RoundedBorder(COLOR_SURFACE_HIGH, 16, 1));
        JLabel servingText = new JLabel("⚖ Serving Size: 420g • Serves 1 adult");
        servingText.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        servingText.setForeground(COLOR_ON_SURFACE);
        servingBadge.add(servingText);
        servingBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(servingBadge);
        right.add(Box.createVerticalStrut(10));

        // 4. Description Paragraph
        JTextArea desc = new JTextArea("Slow-charred organic cottage cheese skewers marinated in hung curd and roasted ground spices, served over fiber-rich brown basmati rice, steamed edamame, and cucumber mint raita.");
        desc.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        desc.setForeground(COLOR_ON_SURFACE_VARIANT);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        right.add(desc);
        right.add(Box.createVerticalStrut(16));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_SURFACE_HIGH);
        right.add(sep);
        right.add(Box.createVerticalStrut(16));

        // ---------------------------------------------------------------------
        // CUSTOMIZATION SECTIONS
        // ---------------------------------------------------------------------

        // Section 1: Choose Your Base (Radio, Required)
        right.add(createSectionHeader("1", "Choose Your Base", "Required • Pick 1", true));
        right.add(Box.createVerticalStrut(8));

        ButtonGroup baseGroup = new ButtonGroup();
        baseBrownRice.setSelected(true);
        baseBrownRice.addActionListener(e -> calculateTotal());
        baseQuinoa.addActionListener(e -> calculateTotal());
        baseCauliflower.addActionListener(e -> calculateTotal());

        baseGroup.add(baseBrownRice);
        baseGroup.add(baseQuinoa);
        baseGroup.add(baseCauliflower);

        JPanel basePanel = new JPanel(new GridLayout(3, 1, 0, 6));
        basePanel.setOpaque(false);
        basePanel.add(styleRadioOption(baseBrownRice, "Slow-cooked whole grain • High dietary fiber", "Included", COLOR_SECONDARY));
        basePanel.add(styleRadioOption(baseQuinoa, "Fluffy tri-color quinoa • Extra 4g protein", "+₹40", COLOR_ON_SURFACE));
        basePanel.add(styleRadioOption(baseCauliflower, "Ultra-low carb • Grated fresh cauliflower", "+₹35", COLOR_ON_SURFACE));
        right.add(basePanel);
        right.add(Box.createVerticalStrut(16));

        // Section 2: Protein Boost (Checkbox, Optional)
        right.add(createSectionHeader("2", "Protein Boost", "Optional • Select multiple", false));
        right.add(Box.createVerticalStrut(8));

        extraPaneer.addActionListener(e -> calculateTotal());
        chickpeas.addActionListener(e -> calculateTotal());

        JPanel proteinPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        proteinPanel.setOpaque(false);
        proteinPanel.add(styleCheckboxOption(extraPaneer, "+₹60"));
        proteinPanel.add(styleCheckboxOption(chickpeas, "+₹30"));
        right.add(proteinPanel);
        right.add(Box.createVerticalStrut(16));

        // Section 3: Sides & Refreshments (Checkbox, Optional)
        right.add(createSectionHeader("3", "Sides & Refreshments", "Optional", false));
        right.add(Box.createVerticalStrut(8));

        extraMintChutney.addActionListener(e -> calculateTotal());
        valenciaJuice.addActionListener(e -> calculateTotal());

        JPanel sidesPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        sidesPanel.setOpaque(false);
        sidesPanel.add(styleCheckboxOption(extraMintChutney, "+₹15"));
        sidesPanel.add(styleCheckboxOption(valenciaJuice, "+₹85"));
        right.add(sidesPanel);
        right.add(Box.createVerticalStrut(16));

        // Section 4: Preparation Notes Input
        JPanel notesHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        notesHeader.setOpaque(false);
        JLabel notesLabel = new JLabel("✎ KITCHEN INSTRUCTIONS (OPTIONAL)");
        notesLabel.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        notesLabel.setForeground(COLOR_ON_SURFACE_VARIANT);
        notesHeader.add(notesLabel);
        right.add(notesHeader);
        right.add(Box.createVerticalStrut(6));

        kitchenNotes.setText("");
        kitchenNotes.setPreferredSize(new Dimension(400, 38));
        kitchenNotes.setBackground(COLOR_SURFACE_LOW);
        kitchenNotes.setForeground(COLOR_ON_SURFACE);
        kitchenNotes.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        kitchenNotes.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        kitchenNotes.setToolTipText("e.g. less spicy, pack raita separately, no green chillies");
        right.add(kitchenNotes);

        return right;
    }

    // =========================================================================
    // 3. STICKY BOTTOM ACTION BAR
    // =========================================================================
    private JPanel createStickyFooter() {
        JPanel footer = new JPanel(new BorderLayout(16, 0));
        footer.setBackground(COLOR_SURFACE_LOWEST);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_SURFACE_HIGH),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));

        // Left: Stepper + Current Unit Price
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftActions.setOpaque(false);

        // Stepper container
        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        stepper.setBackground(COLOR_SURFACE_CONTAINER);
        stepper.setBorder(new RoundedBorder(COLOR_SURFACE_HIGH, 8, 1));

        qtyMinusBtn = new JButton("−");
        styleStepperButton(qtyMinusBtn);
        qtyMinusBtn.setEnabled(false);
        qtyMinusBtn.addActionListener(e -> modifyQuantity(-1));

        qtyDisplay = new JLabel("1", SwingConstants.CENTER);
        qtyDisplay.setFont(new Font(FONT_NAME, Font.BOLD, 15));
        qtyDisplay.setForeground(COLOR_ON_SURFACE);
        qtyDisplay.setPreferredSize(new Dimension(28, 28));

        JButton qtyPlusBtn = new JButton("+");
        styleStepperButton(qtyPlusBtn);
        qtyPlusBtn.addActionListener(e -> modifyQuantity(1));

        stepper.add(qtyMinusBtn);
        stepper.add(qtyDisplay);
        stepper.add(qtyPlusBtn);
        leftActions.add(stepper);

        // Unit price label
        JPanel unitPricePanel = new JPanel();
        unitPricePanel.setLayout(new BoxLayout(unitPricePanel, BoxLayout.Y_AXIS));
        unitPricePanel.setOpaque(false);
        JLabel unitCaption = new JLabel("CURRENT UNIT PRICE");
        unitCaption.setFont(new Font(FONT_NAME, Font.BOLD, 9));
        unitCaption.setForeground(COLOR_ON_SURFACE_VARIANT);
        unitPriceLabel = new JLabel("₹240");
        unitPriceLabel.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        unitPriceLabel.setForeground(COLOR_ON_SURFACE);
        unitPricePanel.add(unitCaption);
        unitPricePanel.add(unitPriceLabel);
        leftActions.add(unitPricePanel);

        footer.add(leftActions, BorderLayout.WEST);

        // Right: CTA Buttons
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        // Primary: Add to Cart & Checkout
        addToCartBtn = new JButton("🛍 Add to Cart • ₹240");
        addToCartBtn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        addToCartBtn.setBackground(COLOR_PRIMARY);
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addToCartBtn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_PRIMARY_CONTAINER, 8, 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        addToCartBtn.addActionListener(e -> {
            int currentTotal = calculateTotal();
            showToast("Added " + quantity + "x Paneer Tikka Bowl to Cart (₹" + currentTotal + ")!");

            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Paneer Tikka Brown Rice Bowl (" + quantity + "x) added to your MealFlow order!\n"
                            + "Would you like to proceed to payment now?",
                    "Cart Updated",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Proceed to Payment", "Keep Customizing"},
                    "Proceed to Payment"
            );
            if (choice == JOptionPane.YES_OPTION) {
                new PaymentPage(customerName, currentTotal).setVisible(true);
                dispose();
            }
        });

        rightActions.add(addToCartBtn);
        footer.add(rightActions, BorderLayout.EAST);

        return footer;
    }

    // =========================================================================
    // DYNAMIC CALCULATION & INTERACTIONS
    // =========================================================================
    private int calculateTotal() {
        int baseAddon = 0;
        if (baseQuinoa.isSelected()) baseAddon = 40;
        else if (baseCauliflower.isSelected()) baseAddon = 35;

        int proteinTotal = 0;
        if (extraPaneer.isSelected()) proteinTotal += 60;
        if (chickpeas.isSelected()) proteinTotal += 30;

        int sidesTotal = 0;
        if (extraMintChutney.isSelected()) sidesTotal += 15;
        if (valenciaJuice.isSelected()) sidesTotal += 85;

        int singleServingTotal = baseDishPrice + baseAddon + proteinTotal + sidesTotal;
        int grandTotal = singleServingTotal * quantity;

        if (unitPriceLabel != null) {
            unitPriceLabel.setText("₹" + singleServingTotal);
        }
        if (addToCartBtn != null) {
            addToCartBtn.setText("🛍 Add to Cart • ₹" + grandTotal);
        }
        return grandTotal;
    }

    private void modifyQuantity(int delta) {
        quantity = Math.max(1, quantity + delta);
        qtyDisplay.setText(String.valueOf(quantity));
        qtyMinusBtn.setEnabled(quantity > 1);
        calculateTotal();
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            favBtn.setText("♥ Saved");
            favBtn.setForeground(COLOR_PRIMARY);
            showToast("Saved Paneer Tikka Bowl to your favourites!");
        } else {
            favBtn.setText("♡ Save");
            favBtn.setForeground(COLOR_ON_SURFACE);
            showToast("Removed from favourites.");
        }
    }

    public void showToast(String message) {
        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }
        toastLabel.setText("  ✓  " + message);
        toastPanel.setVisible(true);
        toastTimer = new Timer(2800, e -> toastPanel.setVisible(false));
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    // =========================================================================
    // HELPER UI BUILDERS & STYLING
    // =========================================================================
    private JPanel createSectionHeader(String number, String title, String subtitle, boolean isRequired) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);

        JLabel numBadge = new JLabel(number, SwingConstants.CENTER);
        numBadge.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        numBadge.setPreferredSize(new Dimension(20, 20));
        numBadge.setOpaque(true);
        numBadge.setBackground(isRequired ? COLOR_PRIMARY : COLOR_SURFACE_HIGHEST);
        numBadge.setForeground(isRequired ? Color.WHITE : COLOR_ON_SURFACE_VARIANT);
        numBadge.setBorder(new RoundedBorder(COLOR_SURFACE_HIGH, 10, 1));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        titleLbl.setForeground(COLOR_ON_SURFACE);

        left.add(numBadge);
        left.add(titleLbl);

        JLabel subLbl = new JLabel(" " + subtitle + " ");
        subLbl.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        subLbl.setOpaque(true);
        if (isRequired) {
            subLbl.setBackground(COLOR_PRIMARY_FIXED);
            subLbl.setForeground(COLOR_ON_PRIMARY_FIXED);
        } else {
            subLbl.setBackground(COLOR_SURFACE_LOW);
            subLbl.setForeground(COLOR_OUTLINE);
        }
        subLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        header.add(left, BorderLayout.WEST);
        header.add(subLbl, BorderLayout.EAST);
        return header;
    }

    private JPanel styleRadioOption(JRadioButton radio, String subtitle, String priceTag, Color priceColor) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(COLOR_SURFACE_LOW);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        radio.setOpaque(false);
        radio.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        radio.setForeground(COLOR_ON_SURFACE);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.add(radio);

        JLabel sub = new JLabel("     " + subtitle);
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        sub.setForeground(COLOR_ON_SURFACE_VARIANT);
        info.add(sub);

        JLabel price = new JLabel(priceTag);
        price.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        price.setForeground(priceColor);

        card.add(info, BorderLayout.CENTER);
        card.add(price, BorderLayout.EAST);

        // Click anywhere to select radio
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio.setSelected(true);
                calculateTotal();
            }
        });
        return card;
    }

    private JPanel styleCheckboxOption(JCheckBox checkbox, String priceTag) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(COLOR_SURFACE_LOW);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 8, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        checkbox.setOpaque(false);
        checkbox.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        checkbox.setForeground(COLOR_ON_SURFACE);

        JLabel price = new JLabel(priceTag);
        price.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        price.setForeground(COLOR_ON_SURFACE);

        card.add(checkbox, BorderLayout.CENTER);
        card.add(price, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                checkbox.setSelected(!checkbox.isSelected());
                calculateTotal();
            }
        });
        return card;
    }

    private JPanel createMacroTile(String name, String value, Color accent, float fillRatio) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(COLOR_SURFACE_LOWEST);
        tile.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_SURFACE_HIGH, 8, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel titleLbl = new JLabel(name);
        titleLbl.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        titleLbl.setForeground(COLOR_ON_SURFACE_VARIANT);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        valLbl.setForeground(accent);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mini bar
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(COLOR_SURFACE_CONTAINER);
                g2.fillRoundRect(0, 0, w, h, 4, 4);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, (int) (w * fillRatio), h, 4, 4);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(50, 4));
        bar.setMaximumSize(new Dimension(80, 4));
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);

        tile.add(titleLbl);
        tile.add(Box.createVerticalStrut(2));
        tile.add(valLbl);
        tile.add(Box.createVerticalStrut(4));
        tile.add(bar);
        return tile;
    }

    private JLabel createTagPill(String text, Color bg, Color fg) {
        JLabel pill = new JLabel(" " + text + " ");
        pill.setFont(new Font(FONT_NAME, Font.BOLD, 10));
        pill.setOpaque(true);
        pill.setBackground(bg);
        pill.setForeground(fg);
        pill.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return pill;
    }

    private JButton createCircleIconButton(String label, String tooltip) {
        JButton btn = new JButton(label);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        btn.setBackground(COLOR_SURFACE_LOW);
        btn.setForeground(COLOR_ON_SURFACE);
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(COLOR_OUTLINE_VARIANT, 14, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return btn;
    }

    private void styleStepperButton(JButton btn) {
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 15));
        btn.setBackground(COLOR_SURFACE_LOWEST);
        btn.setForeground(COLOR_ON_SURFACE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedBorder(COLOR_SURFACE_HIGH, 6, 1));
    }

    private JLabel createClickableLabel(String text, Color normal, Color hover) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        lbl.setForeground(normal);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { lbl.setForeground(hover); }
            @Override public void mouseExited(MouseEvent e) { lbl.setForeground(normal); }
        });
        return lbl;
    }

    // =========================================================================
    // INNER CLASSES: FOOD VISUAL PANEL & MACRO DONUT CHART
    // =========================================================================

    /**
     * FoodVisualPanel draws an artisan culinary bowl illustration with
     * badges for "Bestseller", "Pure Veg", "Plan Eligible", and "100% Organic".
     */
    public static class FoodVisualPanel extends JPanel {
        public FoodVisualPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Shape clip = new RoundRectangle2D.Float(0, 0, w, h, 16, 16);
            g2.setClip(clip);

            BufferedImage img = com.foodapp.util.ImageLoader.getImage("dish_paneer_details.jpg", w, h);
            if (img != null) {
                g2.drawImage(img, 0, 0, w, h, null);
                // Subtle gradient overlay for badge readability
                GradientPaint overlay = new GradientPaint(0, h * 0.5f, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 160));
                g2.setPaint(overlay);
                g2.fillRect(0, 0, w, h);
            } else {
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x3B, 0x22, 0x1A), w, h, new Color(0x19, 0x10, 0x0C));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
            }
            g2.setClip(null);

            // Overlay Top Badges: Bestseller, Pure Veg, Plan Eligible
            int bx = 12;
            int by = 12;

            // Bestseller pill
            g2.setColor(new Color(255, 255, 255, 235));
            g2.fillRoundRect(bx, by, 96, 22, 11, 11);
            g2.setColor(COLOR_PRIMARY);
            g2.setFont(new Font(FONT_NAME, Font.BOLD, 10));
            g2.drawString("🔥 Bestseller", bx + 8, by + 15);

            // Pure Veg pill
            bx += 102;
            g2.setColor(COLOR_SECONDARY_FIXED);
            g2.fillRoundRect(bx, by, 84, 22, 11, 11);
            g2.setColor(COLOR_ON_SECONDARY_FIXED);
            g2.drawString("● Pure Veg", bx + 8, by + 15);

            // Plan Eligible pill
            bx += 90;
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(bx, by, 94, 22, 11, 11);
            g2.setColor(COLOR_SECONDARY);
            g2.drawString("PLAN ELIGIBLE", bx + 8, by + 15);

            // Bottom Ribbon: 100% Organic Dairy & Farm Sourced
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(12, h - 30, 240, 20, 6, 6);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(FONT_NAME, Font.BOLD, 9));
            g2.drawString("🌿 100% Organic Dairy & Farm Sourced", 18, h - 16);

            g2.dispose();
        }
    }

    /**
     * MacroDonutPanel draws an anti-aliased SVG-style donut distribution ring
     * (Protein: Green, Carbs: Orange, Fats: Primary Red).
     */
    public static class MacroDonutPanel extends JPanel {
        private final int proteinPct;
        private final int carbsPct;
        private final int fatsPct;

        public MacroDonutPanel(int proteinPct, int carbsPct, int fatsPct) {
            this.proteinPct = proteinPct;
            this.carbsPct = carbsPct;
            this.fatsPct = fatsPct;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 8;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Background circle track
            g2.setColor(COLOR_SURFACE_HIGHEST);
            g2.drawOval(x, y, size, size);

            double startAngle = 90.0;

            // Protein slice (Green)
            double pAngle = -(proteinPct / 100.0) * 360.0;
            g2.setColor(COLOR_SECONDARY);
            g2.draw(new Arc2D.Double(x, y, size, size, startAngle, pAngle, Arc2D.OPEN));
            startAngle += pAngle;

            // Carbs slice (Orange)
            double cAngle = -(carbsPct / 100.0) * 360.0;
            g2.setColor(COLOR_TERTIARY);
            g2.draw(new Arc2D.Double(x, y, size, size, startAngle, cAngle, Arc2D.OPEN));
            startAngle += cAngle;

            // Fats slice (Primary Red)
            double fAngle = -(fatsPct / 100.0) * 360.0;
            g2.setColor(COLOR_PRIMARY);
            g2.draw(new Arc2D.Double(x, y, size, size, startAngle, fAngle, Arc2D.OPEN));

            g2.dispose();
        }
    }

    /**
     * Custom Rounded Border helper with anti-aliasing.
     */
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
        SwingUtilities.invokeLater(() -> new MealPricingDetailsPage("Rahul").setVisible(true));
    }
}
