package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.AuthValidator;
import com.foodapp.auth.Customer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

/**
 * RegisterPage — High-fidelity Java Swing implementation of the MealFlow Registration UI.
 *
 * Layout mirrors the HTML design:
 *   LEFT  (5 cols): Inspiring culinary lifestyle panel — branding badge, hero food image,
 *                   overlapping subscriber quote card, trust badges (meals delivered, FSSAI).
 *   RIGHT (7 cols): "Create your MealFlow account" heading, SSO buttons (decorative),
 *                   form (full name, email, mobile, password with live strength meter),
 *                   terms checkbox, "Create Account" CTA, sign-in link.
 *
 * Auth logic preserved from original RegisterPage:
 *   AuthService.register() → LoginPage on success.
 */
public class RegisterPage extends JFrame {

    // ── Colour palette ────────────────────────────────────────────────────
    private static final Color C_BG       = new Color(0xF3, 0xFC, 0xF3);
    private static final Color C_SURFACE  = new Color(0xFF, 0xFF, 0xFF);
    private static final Color C_SURF_LOW = new Color(0xED, 0xF6, 0xEE);
    private static final Color C_SURF_CON = new Color(0xE7, 0xF0, 0xE8);
    private static final Color C_SURF_HI  = new Color(0xE2, 0xEA, 0xE2);
    private static final Color C_SURF_HIEST = new Color(0xDC, 0xE5, 0xDD);
    private static final Color C_PRIMARY  = new Color(0xAB, 0x2E, 0x16);
    private static final Color C_PRI_CON  = new Color(0xCE, 0x46, 0x2C);
    private static final Color C_PRI_FIX  = new Color(0xFF, 0xDA, 0xD3);
    private static final Color C_SEC      = new Color(0x00, 0x6C, 0x4A);
    private static final Color C_SEC_FIX  = new Color(0x85, 0xF8, 0xC4);
    private static final Color C_TERTFIX  = new Color(0xFF, 0xDB, 0xCE);
    private static final Color C_TERTFDIM = new Color(0xFF, 0xB5, 0x99);
    private static final Color C_TEXT     = new Color(0x15, 0x1D, 0x19);
    private static final Color C_MUTED    = new Color(0x59, 0x41, 0x3C);
    private static final Color C_OUTLINE  = new Color(0x8D, 0x70, 0x6B);
    private static final Color C_OUTL_V   = new Color(0xE1, 0xBF, 0xB8);
    private static final Color C_ERR      = new Color(0xBA, 0x1A, 0x1A);
    private static final Color C_TERTIARY = new Color(0xA3, 0x39, 0x00);

    // ── Fonts ─────────────────────────────────────────────────────────────
    private static final Font F_DISPLAY = new Font(Font.SANS_SERIF, Font.BOLD, 26);
    private static final Font F_TITLE   = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font F_BODY    = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font F_SMALL   = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font F_LABEL   = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font F_BTN     = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font F_MICRO   = new Font(Font.SANS_SERIF, Font.BOLD, 10);

    // ── Form fields ───────────────────────────────────────────────────────
    private final JTextField     nameField    = new JTextField();
    private final JTextField     emailField   = new JTextField();
    private final JTextField     phoneField   = new JTextField();
    private final JPasswordField passField    = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JCheckBox      termsChk     = new JCheckBox();
    private boolean              pwVisible    = false;
    private JButton              eyeBtn;

    // ── Password strength bars ────────────────────────────────────────────
    private JPanel[] strBars;
    private JLabel   strengthLbl;

    // ── Error label ───────────────────────────────────────────────────────
    private final JLabel errorLabel = new JLabel(" ");

    // ════════════════════════════════════════════════════════════════════════
    public RegisterPage() {
        setTitle("MealFlow — Create Account");
        setSize(1080, 760);
        setMinimumSize(new Dimension(880, 640));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel card = buildCard();
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(C_BG);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ════════════════════════════════════════════════════════════════════════
    // HEADER
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(0xF3, 0xFC, 0xF3, 230));
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, C_OUTL_V),
                BorderFactory.createEmptyBorder(0, 40, 0, 40)));
        h.setPreferredSize(new Dimension(0, 56));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logo.setOpaque(false);
        JLabel logoIcon = new JLabel("🍽"); logoIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        JLabel logoText = new JLabel("MealFlow"); logoText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        logoText.setForeground(C_TEXT);
        logo.add(logoIcon); logo.add(logoText);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel backLbl = new JLabel("← Back to Marketplace"); backLbl.setFont(F_LABEL); backLbl.setForeground(C_MUTED);
        JLabel langLbl = new JLabel("🌐 EN"); langLbl.setFont(F_LABEL); langLbl.setForeground(C_MUTED);
        right.add(backLbl); right.add(langLbl);

        h.add(logo, BorderLayout.WEST); h.add(right, BorderLayout.EAST);
        return h;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN CARD
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(C_SURFACE);
        card.setPreferredSize(new Dimension(1000, 640));
        card.setBorder(BorderFactory.createLineBorder(C_OUTL_V, 1, true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.42;
        card.add(buildLeftPanel(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.58;
        card.add(buildRightPanel(), gbc);

        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    // LEFT PANEL — Lifestyle showcase
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x85, 0xF8, 0xC4, 60));
                g2.fillOval(-80, -80, 280, 280);
                g2.setColor(new Color(0xFF, 0xDA, 0xD3, 60));
                g2.fillOval(getWidth() - 150, getHeight() - 130, 250, 250);
                g2.dispose();
            }
        };
        p.setBackground(C_SURF_LOW);
        p.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // ── Branding badge row ──
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JPanel activeBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        activeBadge.setBackground(C_SEC_FIX);
        activeBadge.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JLabel dot = new JLabel("●"); dot.setFont(F_MICRO); dot.setForeground(C_SEC);
        JLabel badgeTxt = new JLabel("Certified Kitchens · Bengaluru");
        badgeTxt.setFont(F_MICRO); badgeTxt.setForeground(new Color(0x00, 0x21, 0x14));
        activeBadge.add(dot); activeBadge.add(badgeTxt);
        JLabel est = new JLabel("Est. 2024"); est.setFont(F_LABEL); est.setForeground(C_MUTED);
        topRow.add(activeBadge, BorderLayout.WEST); topRow.add(est, BorderLayout.EAST);

        // ── Hero image card ──
        JPanel heroCard = buildHeroImageCard();

        // ── Overlapping subscriber quote ──
        JPanel quoteCard = buildQuoteCard();

        // ── Trust badges ──
        JPanel trust = new JPanel();
        trust.setLayout(new BoxLayout(trust, BoxLayout.Y_AXIS));
        trust.setOpaque(false);
        trust.add(trustBadge("🚚", C_PRI_FIX, "14,200+ Meals", "Delivered weekly across Bengaluru hubs"));
        trust.add(Box.createVerticalStrut(8));
        trust.add(trustBadge("✅", C_SEC_FIX, "Curated & Audited", "Partner kitchens with FSSAI Grade-A certification"));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(topRow);
        center.add(Box.createVerticalStrut(16));
        center.add(heroCard);
        center.add(Box.createVerticalStrut(4));   // overlap quoteCard
        center.add(quoteCard);
        center.add(Box.createVerticalStrut(18));
        center.add(trust);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildHeroImageCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Beautiful gradient background simulating the grain bowl photo
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x6B, 0xB5, 0x6B),
                        getWidth(), getHeight(), new Color(0xC8, 0xE6, 0xBB));
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());
                // Abstract food shapes
                g2.setColor(new Color(0xFF, 0xAA, 0x44, 100));
                g2.fillOval(30, 30, 70, 55);
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 80));
                g2.fillOval(110, 15, 50, 40);
                g2.setColor(new Color(0x44, 0x99, 0x44, 100));
                g2.fillOval(170, 30, 60, 50);
                g2.setColor(new Color(0xFF, 0xDD, 0x88, 80));
                g2.fillOval(60, 70, 80, 60);
                // Dark gradient overlay (bottom)
                GradientPaint overlay = new GradientPaint(0, getHeight(), new Color(0x2A, 0x32, 0x2D, 210), 0, getHeight() / 2, new Color(0, 0, 0, 0));
                g2.setPaint(overlay); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(0, 180));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        card.setBorder(BorderFactory.createLineBorder(C_OUTL_V, 1, true));

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);
        JLabel tag = new JLabel("SEASONAL MACRO BOWL");
        tag.setFont(F_MICRO); tag.setForeground(C_SEC_FIX);
        JLabel name = new JLabel("Harvest Quinoa & Spiced Tempeh");
        name.setFont(F_TITLE); name.setForeground(Color.WHITE);
        JLabel kcal = new JLabel("540 kcal");
        kcal.setFont(F_LABEL); kcal.setForeground(new Color(0xFF, 0xB5, 0x99));
        textStack.add(tag); textStack.add(name); textStack.add(kcal);

        labelPanel.add(textStack, BorderLayout.SOUTH);
        card.add(labelPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuoteCard() {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(new Color(0xFF, 0xFF, 0xFF, 240));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL_V, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel quoteIcon = new JLabel("❝");
        quoteIcon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        quoteIcon.setForeground(C_PRIMARY); quoteIcon.setAlignmentY(TOP_ALIGNMENT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        JLabel qt = new JLabel("<html><span style='color:#151d19;'>&ldquo;MealFlow transformed my weeknights in Indiranagar. Wholesome, chef-calibrated nutrition delivered on the dot.&rdquo;</span></html>");
        qt.setFont(F_SMALL);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        meta.setOpaque(false);
        JLabel avatar = new JLabel("P"); avatar.setFont(F_MICRO); avatar.setForeground(C_MUTED);
        avatar.setOpaque(true); avatar.setBackground(C_SURF_HI);
        avatar.setPreferredSize(new Dimension(20, 20)); avatar.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel authorName = new JLabel("Pooja N."); authorName.setFont(F_LABEL); authorName.setForeground(C_TEXT);
        JLabel role = new JLabel("· Product Lead"); role.setFont(F_SMALL); role.setForeground(C_MUTED);
        meta.add(avatar); meta.add(authorName); meta.add(role);

        textPanel.add(qt); textPanel.add(Box.createVerticalStrut(6)); textPanel.add(meta);
        card.add(quoteIcon, BorderLayout.WEST); card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel trustBadge(String icon, Color iconBg, String title, String subtitle) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(new Color(0xFF, 0xFF, 0xFF, 180));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL_V, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        ico.setOpaque(true); ico.setBackground(iconBg);
        ico.setPreferredSize(new Dimension(40, 40));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2)); text.setOpaque(false);
        JLabel t1 = new JLabel(title); t1.setFont(F_LABEL); t1.setForeground(C_TEXT);
        JLabel t2 = new JLabel("<html><span style='color:#59413c;'>" + subtitle + "</span></html>");
        t2.setFont(F_SMALL);
        text.add(t1); text.add(t2);
        row.add(ico, BorderLayout.WEST); row.add(text, BorderLayout.CENTER);
        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    // RIGHT PANEL — Registration Form
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_SURFACE);
        outer.setBorder(BorderFactory.createEmptyBorder(28, 40, 0, 40));

        JScrollPane scroll = new JScrollPane(buildFormContent());
        scroll.setBorder(null);
        scroll.setBackground(C_SURFACE);
        scroll.getViewport().setBackground(C_SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildFormContent() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SURFACE);

        // ── Heading ──
        JLabel getStarted = new JLabel("GET STARTED");
        getStarted.setFont(F_MICRO); getStarted.setForeground(C_PRIMARY);
        getStarted.setAlignmentX(LEFT_ALIGNMENT);

        JLabel headline = new JLabel("<html><b>Create your MealFlow account</b></html>");
        headline.setFont(F_DISPLAY); headline.setForeground(C_TEXT);
        headline.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><span style='color:#59413c;'>Start ordering on-demand or set up your recurring weekly meal schedule.</span></html>");
        sub.setFont(F_BODY); sub.setAlignmentX(LEFT_ALIGNMENT);

        p.add(getStarted); p.add(Box.createVerticalStrut(4));
        p.add(headline);   p.add(Box.createVerticalStrut(4));
        p.add(sub);        p.add(Box.createVerticalStrut(18));

        // ── SSO buttons (decorative) ──
        JPanel ssoRow = new JPanel(new GridLayout(1, 2, 10, 0));
        ssoRow.setOpaque(false); ssoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        ssoRow.setAlignmentX(LEFT_ALIGNMENT);
        ssoRow.add(ssoBtn("G  Sign up with Google"));
        ssoRow.add(ssoBtn("  Sign up with Apple"));
        p.add(ssoRow); p.add(Box.createVerticalStrut(14));

        // ── Divider ──
        p.add(divider("or register with details")); p.add(Box.createVerticalStrut(14));

        // ── Error ──
        errorLabel.setFont(F_SMALL); errorLabel.setForeground(C_ERR);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(errorLabel); p.add(Box.createVerticalStrut(2));

        // ── Full Name ──
        p.add(fieldLabel("Full Name")); p.add(Box.createVerticalStrut(4));
        styleField(nameField); nameField.setAlignmentX(LEFT_ALIGNMENT);
        p.add(nameField); p.add(Box.createVerticalStrut(12));

        // ── Email ──
        p.add(fieldLabel("Email Address")); p.add(Box.createVerticalStrut(4));
        styleField(emailField); emailField.setAlignmentX(LEFT_ALIGNMENT);
        p.add(emailField); p.add(Box.createVerticalStrut(12));

        // ── Mobile Number ──
        JLabel phoneLbl = new JLabel("<html><b>Mobile Number</b>  <span style='color:#59413c;font-weight:normal;'>(for delivery SMS & WhatsApp updates)</span></html>");
        phoneLbl.setFont(F_LABEL); phoneLbl.setForeground(C_TEXT); phoneLbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(phoneLbl); p.add(Box.createVerticalStrut(4));

        JPanel phoneRow = new JPanel(new BorderLayout(6, 0));
        phoneRow.setOpaque(false); phoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        phoneRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel prefix = new JLabel("🇮🇳 +91", SwingConstants.CENTER);
        prefix.setFont(F_LABEL); prefix.setForeground(C_TEXT); prefix.setOpaque(true);
        prefix.setBackground(C_SURF_LOW);
        prefix.setPreferredSize(new Dimension(76, 44));
        prefix.setBorder(BorderFactory.createLineBorder(C_OUTL_V, 1, true));
        styleField(phoneField); phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        phoneRow.add(prefix, BorderLayout.WEST); phoneRow.add(phoneField, BorderLayout.CENTER);
        p.add(phoneRow); p.add(Box.createVerticalStrut(12));

        // ── Password with strength meter ──
        p.add(fieldLabel("Create Password")); p.add(Box.createVerticalStrut(4));
        JPanel pwWrap = new JPanel(new BorderLayout(0, 0));
        pwWrap.setOpaque(false); pwWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        pwWrap.setAlignmentX(LEFT_ALIGNMENT);
        styleField(passField); passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        eyeBtn = new JButton("👁");
        eyeBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        eyeBtn.setFocusPainted(false); eyeBtn.setBorderPainted(false);
        eyeBtn.setBackground(C_SURF_LOW); eyeBtn.setPreferredSize(new Dimension(44, 44));
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeBtn.addActionListener(e -> {
            pwVisible = !pwVisible;
            passField.setEchoChar(pwVisible ? (char) 0 : '•');
            eyeBtn.setText(pwVisible ? "🙈" : "👁");
        });
        pwWrap.add(passField, BorderLayout.CENTER); pwWrap.add(eyeBtn, BorderLayout.EAST);
        p.add(pwWrap); p.add(Box.createVerticalStrut(6));

        // Strength meter bars
        JPanel meterRow = new JPanel(new GridLayout(1, 4, 6, 0));
        meterRow.setOpaque(false); meterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        meterRow.setAlignmentX(LEFT_ALIGNMENT);
        strBars = new JPanel[4];
        for (int i = 0; i < 4; i++) {
            strBars[i] = new JPanel();
            strBars[i].setBackground(C_SURF_HIEST);
            strBars[i].setPreferredSize(new Dimension(0, 6));
            meterRow.add(strBars[i]);
        }
        p.add(meterRow); p.add(Box.createVerticalStrut(4));

        JPanel meterLabel = new JPanel(new BorderLayout());
        meterLabel.setOpaque(false); meterLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        meterLabel.setAlignmentX(LEFT_ALIGNMENT);
        strengthLbl = new JLabel("Security indicator"); strengthLbl.setFont(F_MICRO); strengthLbl.setForeground(C_MUTED);
        JLabel reqLbl = new JLabel("Requires 8+ chars & 1 symbol/number"); reqLbl.setFont(F_MICRO); reqLbl.setForeground(C_OUTLINE);
        meterLabel.add(strengthLbl, BorderLayout.WEST); meterLabel.add(reqLbl, BorderLayout.EAST);
        p.add(meterLabel); p.add(Box.createVerticalStrut(12));

        // Hook strength meter to password field
        passField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStrength(); }
            public void removeUpdate(DocumentEvent e) { updateStrength(); }
            public void changedUpdate(DocumentEvent e) { updateStrength(); }
        });

        // ── Confirm Password ──
        p.add(fieldLabel("Confirm Password")); p.add(Box.createVerticalStrut(4));
        styleField(confirmField); confirmField.setAlignmentX(LEFT_ALIGNMENT);
        p.add(confirmField); p.add(Box.createVerticalStrut(12));

        // ── Terms checkbox ──
        JPanel termsRow = new JPanel(new BorderLayout(8, 0));
        termsRow.setOpaque(false); termsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        termsRow.setAlignmentX(LEFT_ALIGNMENT);
        termsChk.setOpaque(false);
        JLabel termsLbl = new JLabel("<html><span style='color:#59413c;'>I agree to MealFlow's <b style='color:#ab2e16;'>Terms of Service</b> and <b style='color:#ab2e16;'>Privacy Policy</b>. I consent to order status dispatches via WhatsApp &amp; SMS.</span></html>");
        termsLbl.setFont(F_SMALL);
        termsRow.add(termsChk, BorderLayout.WEST); termsRow.add(termsLbl, BorderLayout.CENTER);
        p.add(termsRow); p.add(Box.createVerticalStrut(16));

        // ── Create Account button ──
        JButton createBtn = new JButton("Create Account & Get Started  →");
        styleBtn(createBtn); createBtn.setAlignmentX(LEFT_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        createBtn.addActionListener(e -> doRegister());
        p.add(createBtn); p.add(Box.createVerticalStrut(20));

        // ── Sign-in link ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false); footer.setAlignmentX(LEFT_ALIGNMENT);
        JLabel siLbl = new JLabel("Already have an account?");
        siLbl.setFont(F_BODY); siLbl.setForeground(C_TEXT);
        JLabel siLink = new JLabel("<html><u><b><a href='#' style='color:#ab2e16;'>Sign in here</a></b></u></html>");
        siLink.setFont(F_LABEL); siLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        siLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { new LoginPage().setVisible(true); dispose(); }
        });
        footer.add(siLbl); footer.add(siLink);

        JLabel exploreNote = new JLabel("🧭  You can always explore the marketplace first without creating a subscription.");
        exploreNote.setFont(F_MICRO); exploreNote.setForeground(C_MUTED);
        exploreNote.setAlignmentX(CENTER_ALIGNMENT);

        p.add(footer); p.add(Box.createVerticalStrut(4));
        p.add(exploreNote); p.add(Box.createVerticalStrut(20));

        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    // FOOTER
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setBackground(C_BG);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, C_OUTL_V),
                BorderFactory.createEmptyBorder(10, 40, 10, 40)));
        JLabel left = new JLabel("© 2025 MealFlow Culinary OS. Nourishing and intentional living.");
        left.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11)); left.setForeground(C_MUTED);
        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        links.setOpaque(false);
        for (String lnk : new String[]{"Privacy Policy", "Terms of Service", "Assistance"}) {
            JLabel l = new JLabel(lnk); l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            l.setForeground(C_MUTED); l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            links.add(l);
        }
        f.add(left, BorderLayout.WEST); f.add(links, BorderLayout.EAST);
        return f;
    }

    // ════════════════════════════════════════════════════════════════════════
    // AUTH LOGIC (unchanged from original)
    // ════════════════════════════════════════════════════════════════════════
    private void doRegister() {
        errorLabel.setText(" ");
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String phone   = phoneField.getText().trim().replaceAll("\\s+", "");
        String pw      = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());

        String error = null;
        if (!AuthValidator.isNotEmpty(name))         error = "Full name is required.";
        else if (!AuthValidator.isValidEmail(email)) error = "Invalid email address.";
        else if (!AuthValidator.isValidPhone(phone)) error = "Phone must be exactly 10 digits.";
        else if (!AuthValidator.isValidPassword(pw)) error = "Password must be at least 6 characters.";
        else if (!pw.equals(confirm))                error = "Passwords do not match.";
        else if (!termsChk.isSelected())             error = "You must agree to the Terms of Service.";

        if (error != null) { errorLabel.setText(error); return; }

        if (AuthService.register(new Customer(name, email, phone, pw))) {
            JOptionPane.showMessageDialog(this, "Account created! Please sign in.", "Welcome to MealFlow", JOptionPane.INFORMATION_MESSAGE);
            new LoginPage().setVisible(true);
            dispose();
        } else {
            errorLabel.setText("This email is already registered. Please sign in.");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // LIVE PASSWORD STRENGTH METER
    // ════════════════════════════════════════════════════════════════════════
    private void updateStrength() {
        String val = new String(passField.getPassword());
        int score = 0;
        if (val.length() >= 6)  score++;
        if (val.length() >= 8)  score++;
        if (val.matches(".*[0-9].*") || val.matches(".*[^A-Za-z0-9].*")) score++;
        if (val.length() >= 10 && val.matches(".*[A-Z].*") && val.matches(".*[0-9].*") && val.matches(".*[^A-Za-z0-9].*")) score++;

        for (JPanel bar : strBars) bar.setBackground(C_SURF_HIEST);

        if (val.isEmpty()) {
            strengthLbl.setText("Security indicator"); strengthLbl.setForeground(C_MUTED);
        } else if (score <= 1) {
            strBars[0].setBackground(C_ERR);
            strengthLbl.setText("Weak password"); strengthLbl.setForeground(C_ERR);
        } else if (score == 2) {
            strBars[0].setBackground(C_TERTIARY); strBars[1].setBackground(C_TERTIARY);
            strengthLbl.setText("Moderate"); strengthLbl.setForeground(C_TERTIARY);
        } else if (score == 3) {
            strBars[0].setBackground(C_SEC); strBars[1].setBackground(C_SEC); strBars[2].setBackground(C_SEC);
            strengthLbl.setText("Strong password"); strengthLbl.setForeground(C_SEC);
        } else {
            for (JPanel bar : strBars) bar.setBackground(C_SEC);
            strengthLbl.setText("Excellent security"); strengthLbl.setForeground(C_SEC);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // SHARED HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private void styleField(JTextField f) {
        f.setFont(F_BODY); f.setBackground(C_SURF_LOW); f.setForeground(C_TEXT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL_V, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBackground(C_SURFACE); }
            public void focusLost(FocusEvent e)   { f.setBackground(C_SURF_LOW); }
        });
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(F_LABEL); l.setForeground(C_TEXT);
        l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }

    private JButton ssoBtn(String text) {
        JButton b = new JButton(text); b.setFont(F_LABEL);
        b.setBackground(C_SURF_LOW); b.setForeground(C_TEXT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL_V, 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(C_SURF_CON); }
            public void mouseExited(MouseEvent e)  { b.setBackground(C_SURF_LOW); }
        });
        return b;
    }

    private JComponent divider(String text) {
        JPanel div = new JPanel(new BorderLayout(8, 0));
        div.setOpaque(false); div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        div.setAlignmentX(LEFT_ALIGNMENT);
        JPanel lineL = new JPanel(); lineL.setBackground(C_SURF_HI); lineL.setPreferredSize(new Dimension(0, 1));
        JPanel lineR = new JPanel(); lineR.setBackground(C_SURF_HI); lineR.setPreferredSize(new Dimension(0, 1));
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(F_MICRO); lbl.setForeground(C_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        div.add(lineL, BorderLayout.WEST); div.add(lbl, BorderLayout.CENTER); div.add(lineR, BorderLayout.EAST);
        return div;
    }

    private void styleBtn(JButton b) {
        b.setFont(F_BTN); b.setBackground(C_PRIMARY); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(C_PRI_CON); }
            public void mouseExited(MouseEvent e)  { b.setBackground(C_PRIMARY); }
        });
    }
}
