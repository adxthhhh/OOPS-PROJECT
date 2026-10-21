package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.AuthValidator;
import com.foodapp.auth.Customer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * LoginPage — High-fidelity Java Swing implementation of the MealFlow Login UI.
 *
 * Layout mirrors the HTML design:
 *   LEFT  (5 cols): Culinary showcase hero panel with perks, quote, decorative glows.
 *   RIGHT (7 cols): "Welcome back" heading, SSO buttons (decorative), email + password
 *                   form with show/hide toggle, remember-me, sign-in CTA, register link.
 *
 * Auth logic is preserved from the original LoginPage:
 *   AuthService.login() → HomePage on success.
 */
public class LoginPage extends JFrame {

    // ── Colour palette (matches HTML Tailwind config) ──────────────────────
    private static final Color C_BG       = new Color(0xF3, 0xFC, 0xF3); // background
    private static final Color C_SURFACE  = new Color(0xFF, 0xFF, 0xFF); // surface-container-lowest
    private static final Color C_SURF_LOW = new Color(0xED, 0xF6, 0xEE); // surface-container-low
    private static final Color C_SURF_CON = new Color(0xE7, 0xF0, 0xE8); // surface-container
    private static final Color C_SURF_HI  = new Color(0xE2, 0xEA, 0xE2); // surface-container-high
    private static final Color C_PRIMARY  = new Color(0xAB, 0x2E, 0x16); // primary
    private static final Color C_PRI_CON  = new Color(0xCE, 0x46, 0x2C); // primary-container
    private static final Color C_PRI_FIX  = new Color(0xFF, 0xDA, 0xD3); // primary-fixed
    private static final Color C_SEC      = new Color(0x00, 0x6C, 0x4A); // secondary
    private static final Color C_SEC_FIX  = new Color(0x85, 0xF8, 0xC4); // secondary-fixed
    private static final Color C_TERTFIX  = new Color(0xFF, 0xDB, 0xCE); // tertiary-fixed
    private static final Color C_TEXT     = new Color(0x15, 0x1D, 0x19); // on-surface
    private static final Color C_MUTED    = new Color(0x59, 0x41, 0x3C); // on-surface-variant
    private static final Color C_OUTLINE  = new Color(0x8D, 0x70, 0x6B); // outline
    private static final Color C_OUTL_V   = new Color(0xE1, 0xBF, 0xB8); // outline-variant
    private static final Color C_ERR      = new Color(0xBA, 0x1A, 0x1A); // error

    // ── Fonts ───────────────────────────────────────────────────────────────
    private static final Font F_DISPLAY = new Font(Font.SANS_SERIF, Font.BOLD, 28);
    private static final Font F_TITLE   = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font F_BODY    = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font F_SMALL   = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font F_LABEL   = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font F_BTN     = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font F_MICRO   = new Font(Font.SANS_SERIF, Font.BOLD, 10);

    // ── Form fields ─────────────────────────────────────────────────────────
    private final JTextField     emailField   = new JTextField();
    private final JPasswordField passField    = new JPasswordField();
    private final JCheckBox      rememberChk  = new JCheckBox("Keep me signed in on this device");
    private JButton              eyeToggleBtn;
    private boolean              pwVisible    = false;

    // ── Error label ─────────────────────────────────────────────────────────
    private final JLabel errorLabel = new JLabel(" ");

    // ════════════════════════════════════════════════════════════════════════
    public LoginPage() {
        setTitle("MealFlow — Sign In");
        setSize(1060, 700);
        setMinimumSize(new Dimension(860, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);

        // Top header bar
        add(buildHeader(), BorderLayout.NORTH);

        // Main card (left + right panels)
        JPanel card = buildCard();
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(C_BG);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        // Footer
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

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logo.setOpaque(false);
        JLabel logoIcon = new JLabel("🍽");
        logoIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        JLabel logoText = new JLabel("MealFlow");
        logoText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        logoText.setForeground(C_TEXT);
        logo.add(logoIcon); logo.add(logoText);

        // Right side: back + lang
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel backLbl = new JLabel("← Back to Marketplace");
        backLbl.setFont(F_LABEL); backLbl.setForeground(C_MUTED);
        backLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel sep = new JPanel(); sep.setBackground(C_OUTL_V);
        sep.setPreferredSize(new Dimension(1, 16));
        JLabel langLbl = new JLabel("🌐 EN");
        langLbl.setFont(F_LABEL); langLbl.setForeground(C_MUTED);
        right.add(backLbl); right.add(sep); right.add(langLbl);

        h.add(logo, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN CARD
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(C_SURFACE);
        card.setPreferredSize(new Dimension(980, 600));
        card.setBorder(BorderFactory.createLineBorder(C_OUTL_V, 1, true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left panel — 5/12 width
        gbc.gridx = 0; gbc.weightx = 0.42;
        card.add(buildLeftPanel(), gbc);

        // Right panel — 7/12 width
        gbc.gridx = 1; gbc.weightx = 0.58;
        card.add(buildRightPanel(), gbc);

        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    // LEFT PANEL — Culinary showcase
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Decorative radial glows
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Top-left glow (secondary-container tint)
                g2.setColor(new Color(0x82, 0xF5, 0xC1, 55));
                g2.fillOval(-80, -80, 300, 300);
                // Bottom-right glow (primary-fixed tint)
                g2.setColor(new Color(0xFF, 0xDA, 0xD3, 70));
                g2.fillOval(getWidth() - 160, getHeight() - 140, 260, 260);
                g2.dispose();
            }
        };
        p.setBackground(C_SURF_LOW);
        p.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // ── TOP: Sub-brand badge ──
        JPanel badge = pillBadge("🌿  Culinary Operating System");
        badge.setAlignmentX(LEFT_ALIGNMENT);

        // ── CENTER: Hero image card ──
        JPanel heroCard = buildHeroCard();

        // ── Editorial pitch ──
        JLabel headline = new JLabel("<html><b>Healthy eating on auto-pilot.</b></html>");
        headline.setFont(F_TITLE); headline.setForeground(C_TEXT);

        JLabel body = new JLabel("<html><span style='color:#59413c;'>Discover neighborhood kitchens or build a weekly flexible meal routine tailored to your nutrition goals.</span></html>");
        body.setFont(F_BODY);

        // ── PERKS ──
        JPanel perks = new JPanel();
        perks.setLayout(new BoxLayout(perks, BoxLayout.Y_AXIS));
        perks.setOpaque(false);
        perks.add(perkRow("💰", C_SEC_FIX, "Zero hidden delivery fees", "Upfront pricing, every billing cycle"));
        perks.add(Box.createVerticalStrut(8));
        perks.add(perkRow("📅", C_PRI_FIX, "Cancel or skip before 8 PM", "Total flexibility for unplanned dinners"));
        perks.add(Box.createVerticalStrut(8));
        perks.add(perkRow("🏅", C_SEC_FIX, "Curated hygienic cloud kitchens", "Vetted nutrition & clean-label sourcing"));

        // ── Trust quote ──
        JPanel quoteStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quoteStrip.setOpaque(false);
        JLabel avatar = new JLabel("M");
        avatar.setFont(F_BTN); avatar.setForeground(new Color(0x37, 0x0E, 0x00));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setOpaque(true); avatar.setBackground(C_TERTFIX);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JLabel quote = new JLabel("<html><i style='color:#59413c;'>&ldquo;MealFlow removed the daily decision fatigue of cooking after 10-hour days.&rdquo;</i></html>");
        quote.setFont(F_SMALL);
        quoteStrip.add(avatar); quoteStrip.add(quote);

        // Assemble top section
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(badge);
        top.add(Box.createVerticalStrut(18));
        top.add(headline);
        top.add(Box.createVerticalStrut(6));
        top.add(body);
        top.add(Box.createVerticalStrut(18));
        top.add(heroCard);
        top.add(Box.createVerticalStrut(20));
        top.add(perks);

        p.add(top, BorderLayout.CENTER);
        p.add(quoteStrip, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildHeroCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(0xE7, 0xF0, 0xE8));
        card.setBorder(BorderFactory.createLineBorder(C_OUTL_V, 1, true));
        card.setPreferredSize(new Dimension(0, 180));

        // Gradient overlay label simulating the photo card
        JPanel overlay = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Simulate a dark-to-light gradient like the HTML image overlay
                GradientPaint gp = new GradientPaint(0, getHeight(), new Color(0x2A, 0x32, 0x2D, 200), 0, 0, new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);
        overlay.setBorder(BorderFactory.createEmptyBorder(0, 14, 12, 14));

        JPanel textOverlay = new JPanel(new BorderLayout());
        textOverlay.setOpaque(false);
        JLabel tag = new JLabel("FEATURED CLOUD KITCHEN");
        tag.setFont(F_MICRO); tag.setForeground(C_SEC_FIX);
        JLabel name = new JLabel("The Sage & Hearth Collective");
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14)); name.setForeground(Color.WHITE);
        JLabel rating = new JLabel("★ 4.9 (1,240+ subscribers)");
        rating.setFont(F_SMALL); rating.setForeground(new Color(0xFF, 0xB5, 0x99));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);
        textStack.add(tag); textStack.add(name); textStack.add(rating);

        overlay.add(textStack, BorderLayout.SOUTH);
        card.add(overlay, BorderLayout.CENTER);

        // Tinted background to suggest the food image
        JPanel bg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x82, 0xC4, 0x7A), getWidth(), getHeight(), new Color(0xCC, 0xE8, 0xCC));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Draw abstract food-like shapes
                g2.setColor(new Color(0x60, 0xAA, 0x60, 80));
                g2.fillOval(20, 20, 80, 60);
                g2.setColor(new Color(0xFF, 0xAA, 0x44, 80));
                g2.fillOval(100, 40, 60, 50);
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 50));
                g2.fillOval(160, 10, 50, 40);
                g2.dispose();
            }
        };
        bg.setPreferredSize(new Dimension(0, 180));
        bg.add(overlay);
        bg.setLayout(new BorderLayout());
        bg.add(overlay, BorderLayout.CENTER);
        return bg;
    }

    // ════════════════════════════════════════════════════════════════════════
    // RIGHT PANEL — Login Form
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SURFACE);
        p.setBorder(BorderFactory.createEmptyBorder(36, 44, 36, 44));

        // ── Header group ──
        JLabel tag = new JLabel("🔓  SECURE ACCESS");
        tag.setFont(F_MICRO); tag.setForeground(C_PRIMARY);
        tag.setAlignmentX(LEFT_ALIGNMENT);

        JLabel headline = new JLabel("<html><b>Welcome back to MealFlow</b></html>");
        headline.setFont(F_DISPLAY); headline.setForeground(C_TEXT);
        headline.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html><span style='color:#59413c;'>Sign in to manage your meal subscriptions, upcoming orders, and kitchen favorites.</span></html>");
        sub.setFont(F_BODY); sub.setAlignmentX(LEFT_ALIGNMENT);

        p.add(tag); p.add(Box.createVerticalStrut(6));
        p.add(headline); p.add(Box.createVerticalStrut(6));
        p.add(sub); p.add(Box.createVerticalStrut(22));

        // ── SSO buttons (decorative) ──
        JPanel ssoRow = new JPanel(new GridLayout(1, 2, 10, 0));
        ssoRow.setOpaque(false); ssoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        ssoRow.setAlignmentX(LEFT_ALIGNMENT);
        ssoRow.add(ssoBtn("G  Sign in with Google"));
        ssoRow.add(ssoBtn("  Sign in with Apple"));
        p.add(ssoRow); p.add(Box.createVerticalStrut(16));

        // ── Divider ──
        p.add(divider("or sign in with email / phone")); p.add(Box.createVerticalStrut(16));

        // ── Error label ──
        errorLabel.setFont(F_SMALL); errorLabel.setForeground(C_ERR);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(errorLabel); p.add(Box.createVerticalStrut(4));

        // ── Email field ──
        p.add(fieldLabel("Email address or Mobile Number")); p.add(Box.createVerticalStrut(5));
        emailField.setFont(F_BODY); emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        emailField.setBackground(C_SURF_LOW); emailField.setForeground(C_TEXT);
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL_V, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        emailField.setAlignmentX(LEFT_ALIGNMENT);
        emailField.putClientProperty("JTextField.placeholderText", "e.g. rahul.sharma@example.com");
        p.add(emailField); p.add(Box.createVerticalStrut(14));

        // ── Password field with show/hide ──
        JPanel pwRow = new JPanel(new BorderLayout());
        pwRow.setOpaque(false); pwRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        pwRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel pwLbl = fieldLabel("Password"); pwLbl.setAlignmentX(LEFT_ALIGNMENT);
        JLabel forgotLbl = new JLabel("<html><a href='#' style='color:#ab2e16;'>Forgot password?</a></html>");
        forgotLbl.setFont(F_LABEL); forgotLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pwRow.add(pwLbl, BorderLayout.WEST); pwRow.add(forgotLbl, BorderLayout.EAST);
        p.add(pwRow); p.add(Box.createVerticalStrut(5));

        JPanel pwWrap = new JPanel(new BorderLayout());
        pwWrap.setOpaque(false); pwWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        pwWrap.setAlignmentX(LEFT_ALIGNMENT);
        passField.setFont(F_BODY); passField.setBackground(C_SURF_LOW); passField.setForeground(C_TEXT);
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL_V, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        eyeToggleBtn = new JButton("👁");
        eyeToggleBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        eyeToggleBtn.setFocusPainted(false); eyeToggleBtn.setBorderPainted(false);
        eyeToggleBtn.setBackground(C_SURF_LOW); eyeToggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeToggleBtn.setPreferredSize(new Dimension(44, 44));
        eyeToggleBtn.addActionListener(e -> togglePassword());
        pwWrap.add(passField, BorderLayout.CENTER); pwWrap.add(eyeToggleBtn, BorderLayout.EAST);
        p.add(pwWrap); p.add(Box.createVerticalStrut(12));

        // ── Remember me ──
        rememberChk.setFont(F_BODY); rememberChk.setOpaque(false);
        rememberChk.setForeground(C_TEXT); rememberChk.setSelected(true);
        rememberChk.setAlignmentX(LEFT_ALIGNMENT);
        p.add(rememberChk); p.add(Box.createVerticalStrut(18));

        // ── Sign In button ──
        JButton signInBtn = new JButton("Sign In  →");
        styleBtn(signInBtn);
        signInBtn.setAlignmentX(LEFT_ALIGNMENT);
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        signInBtn.addActionListener(e -> doLogin());
        // Also submit on Enter
        passField.addActionListener(e -> doLogin());
        p.add(signInBtn); p.add(Box.createVerticalStrut(20));

        // ── Register link ──
        JPanel regLink = new JPanel(new FlowLayout(FlowLayout.CENTER));
        regLink.setOpaque(false); regLink.setAlignmentX(LEFT_ALIGNMENT);
        JLabel regLbl = new JLabel("Don't have an account?");
        regLbl.setFont(F_BODY); regLbl.setForeground(C_MUTED);
        JLabel regBtn2 = new JLabel("<html><u><b><a href='#' style='color:#ab2e16;'>Create an account / Get Started</a></b></u></html>");
        regBtn2.setFont(F_LABEL); regBtn2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regBtn2.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { new RegisterPage().setVisible(true); dispose(); }
        });
        regLink.add(regLbl); regLink.add(regBtn2);
        p.add(regLink); p.add(Box.createVerticalStrut(14));

        // ── Security micro-badge ──
        JLabel secBadge = new JLabel("🔒  256-bit encrypted · We never post without permission");
        secBadge.setFont(F_MICRO); secBadge.setForeground(C_MUTED);
        secBadge.setAlignmentX(CENTER_ALIGNMENT);
        p.add(secBadge);

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
        left.setFont(F_SMALL); left.setForeground(C_MUTED);
        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        links.setOpaque(false);
        for (String lnk : new String[]{"Privacy Policy", "Terms of Service", "Assistance"}) {
            JLabel l = new JLabel(lnk); l.setFont(F_SMALL); l.setForeground(C_MUTED);
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            links.add(l);
        }
        f.add(left, BorderLayout.WEST); f.add(links, BorderLayout.EAST);
        return f;
    }

    // ════════════════════════════════════════════════════════════════════════
    // AUTH LOGIC  (unchanged from original)
    // ════════════════════════════════════════════════════════════════════════
    private void doLogin() {
        errorLabel.setText(" ");
        String email = emailField.getText().trim();
        String pw    = new String(passField.getPassword());

        if (!AuthValidator.isNotEmpty(email) || !AuthValidator.isNotEmpty(pw)) {
            errorLabel.setText("Please fill in both fields.");
            return;
        }
        if (!AuthValidator.isValidEmail(email)) {
            errorLabel.setText("Invalid email format.");
            return;
        }
        Customer c = AuthService.login(email, pw);
        if (c == null) {
            errorLabel.setText("Wrong email or password. Please try again.");
            passField.setText("");
        } else {
            new HomePage(c.getName()).setVisible(true);
            dispose();
        }
    }

    private void togglePassword() {
        pwVisible = !pwVisible;
        passField.setEchoChar(pwVisible ? (char) 0 : '•');
        eyeToggleBtn.setText(pwVisible ? "🙈" : "👁");
    }

    // ════════════════════════════════════════════════════════════════════════
    // SHARED HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private JPanel pillBadge(String text) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        badge.setBackground(C_SEC_FIX);
        badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        badge.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JLabel lbl = new JLabel(text);
        lbl.setFont(F_MICRO); lbl.setForeground(new Color(0x00, 0x21, 0x14));
        badge.add(lbl);
        return badge;
    }

    private JPanel perkRow(String icon, Color iconBg, String title, String subtitle) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(0xFF, 0xFF, 0xFF, 180));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL_V, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        iconLbl.setOpaque(true); iconLbl.setBackground(iconBg);
        iconLbl.setPreferredSize(new Dimension(36, 36));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setOpaque(false);
        JLabel t1 = new JLabel(title); t1.setFont(F_LABEL); t1.setForeground(C_TEXT);
        JLabel t2 = new JLabel(subtitle); t2.setFont(F_SMALL); t2.setForeground(C_MUTED);
        text.add(t1); text.add(t2);

        row.add(iconLbl, BorderLayout.WEST); row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JButton ssoBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_LABEL); b.setBackground(C_SURF_LOW); b.setForeground(C_TEXT);
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
        JPanel lineL = new JPanel(); lineL.setBackground(C_OUTL_V); lineL.setPreferredSize(new Dimension(0, 1));
        JPanel lineR = new JPanel(); lineR.setBackground(C_OUTL_V); lineR.setPreferredSize(new Dimension(0, 1));
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(F_MICRO); lbl.setForeground(C_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        div.add(lineL, BorderLayout.WEST); div.add(lbl, BorderLayout.CENTER); div.add(lineR, BorderLayout.EAST);
        return div;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL); l.setForeground(C_TEXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
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

    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        UIStyle.setup();
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
