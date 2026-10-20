package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.Customer;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * UIStyle handles the global look-and-feel, color schemes, typography,
 * and component styling across the food delivery desktop application.
 *
 * <p>Integrates with FlatLaf for modern UI components while maintaining
 * backward-compatibility for existing screens.</p>
 */
public final class UIStyle {

    // =========================================================================
    // 1. COLOR CONSTANTS
    // =========================================================================

    /** Primary Accent — Forest Green */
    public static final Color PRIMARY         = new Color(0x2E7D32);

    /** Primary Hover Color */
    public static final Color PRIMARY_HOVER   = new Color(0x388E3C);

    /** Primary Pressed/Active Color */
    public static final Color PRIMARY_PRESSED = new Color(0x1B5E20);

    /** Window & Screen Background */
    public static final Color BG              = new Color(0xF4F6F4);

    /** Card & Container Background */
    public static final Color CARD_BG         = new Color(0xFFFFFF);

    /** Primary Text (Dark Charcoal) */
    public static final Color TEXT_MAIN       = new Color(0x212121);

    /** Secondary & Muted Text (Slate/Gray) */
    public static final Color TEXT_SECONDARY  = new Color(0x757575);

    /** Light Border Color for cards and input outlines */
    public static final Color BORDER          = new Color(0xDDDDDD);

    /** Success / Active Accent Color */
    public static final Color SUCCESS         = new Color(0x2E7D32);

    /** Danger / Error Accent Color */
    public static final Color DANGER          = new Color(0xD32F2F);

    // =========================================================================
    // 2. CUSTOM FONTS & TYPOGRAPHY
    // =========================================================================

    private static final String FONT_FAMILY = detectSystemSafeFont();

    /** Heading: Page titles and prominent banners (24 pt, Bold) */
    public static final Font FONT_HEADING    = new Font(FONT_FAMILY, Font.BOLD, 24);

    /** Subheading: Section headers and card titles (16 pt, Bold) */
    public static final Font FONT_SUBHEADING = new Font(FONT_FAMILY, Font.BOLD, 16);

    /** Body: Standard reading and form text (14 pt, Plain) */
    public static final Font FONT_BODY       = new Font(FONT_FAMILY, Font.PLAIN, 14);

    /** Small: Captions, helper text, and tags (12 pt, Plain) */
    public static final Font FONT_SMALL      = new Font(FONT_FAMILY, Font.PLAIN, 12);

    // Backward-compatible font aliases for existing modules
    public static final Font TITLE   = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font HEADING = FONT_SUBHEADING;
    public static final Font BODY    = FONT_BODY;

    // =========================================================================
    // 3. FLATLAF INITIALIZATION
    // =========================================================================

    /**
     * Initializes FlatLaf Look and Feel with theme selection and global UI styling defaults.
     *
     * @param useDarkMode true for FlatDarkLaf, false for FlatLightLaf
     */
    public static void setup(boolean useDarkMode) {
        try {
            if (useDarkMode) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            // Global UI Manager properties for smooth, modern styling
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("Table.rowHeight", 30);
            UIManager.put("defaultFont", FONT_BODY);

        } catch (Throwable t) {
            System.err.println("[UIStyle] Warning: Could not initialize FlatLaf. Falling back to system look and feel: " + t.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Initializes FlatLaf using the default light theme.
     */
    public static void setup() {
        setup(false);
    }

    // =========================================================================
    // 4. COMPONENT STYLING HELPERS
    // =========================================================================

    /**
     * Styles a standard {@link JButton} with rounded corners, responsive hover/press states,
     * and modern flat padding without harsh default borders.
     *
     * @param button    the button to style
     * @param isPrimary true for solid primary accent button, false for secondary outline button
     */
    public static void styleButton(JButton button, boolean isPrimary) {
        button.setFont(FONT_SUBHEADING);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // FlatLaf client properties for rounded geometry
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE,
                isPrimary ? FlatClientProperties.BUTTON_TYPE_ROUND_RECT : FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 10,20,10,20");

        if (isPrimary) {
            button.setBackground(PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (button.isEnabled()) button.setBackground(PRIMARY_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (button.isEnabled()) button.setBackground(PRIMARY);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (button.isEnabled()) button.setBackground(PRIMARY_PRESSED);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (button.isEnabled()) {
                        button.setBackground(button.contains(e.getPoint()) ? PRIMARY_HOVER : PRIMARY);
                    }
                }
            });
        } else {
            button.setBackground(CARD_BG);
            button.setForeground(PRIMARY);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 1, true),
                    BorderFactory.createEmptyBorder(9, 21, 9, 21)
            ));

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (button.isEnabled()) button.setBackground(new Color(0xE8F5E9));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (button.isEnabled()) button.setBackground(CARD_BG);
                }
            });
        }
    }

    /**
     * Styles a {@link JTextField} with clean borders, interior padding, and
     * placeholder text using FlatLaf's client property support.
     *
     * @param field       the text field to style
     * @param placeholder placeholder text displayed when field is empty
     */
    public static void styleTextField(JTextField field, String placeholder) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_MAIN);
        field.setBackground(CARD_BG);

        // Native placeholder support in FlatLaf
        if (placeholder != null && !placeholder.trim().isEmpty()) {
            field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        }

        field.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 8,12,8,12");

        // Fallback compound border for consistent padding across look-and-feels
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    /**
     * Styles a {@link JPanel} as a modern elevated card container with rounded corners,
     * clean white background, and balanced padding.
     *
     * @param panel the card container panel to style
     */
    public static void styleCardPanel(JPanel panel) {
        panel.setBackground(CARD_BG);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #FFFFFF");

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
    }

    // =========================================================================
    // 5. LEGACY HELPER METHODS (Preserved for compatibility)
    // =========================================================================

    /**
     * Backward-compatible convenience factory for standard primary buttons.
     */
    public static JButton button(String text) {
        JButton b = new JButton(text);
        styleButton(b, true);
        return b;
    }

    /**
     * Backward-compatible header panel generator with title text.
     * Automatically includes a profile button in the top right.
     */
    public static JPanel header(String text) {
        Customer c = AuthService.getCurrentUser();
        String name = (c != null) ? c.getName() : "Profile";
        return headerWithProfile(text, null, name);
    }

    /**
     * Creates a modern header with title on the left and a profile icon button on the top right.
     *
     * @param text         Header title string
     * @param currentFrame Current JFrame window (disposed on navigation, or null to auto-detect)
     * @param customerName Customer name to display on profile button
     * @return Styled JPanel header
     */
    public static JPanel headerWithProfile(String text, JFrame currentFrame, String customerName) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel l = new JLabel(text);
        l.setFont(TITLE);
        l.setForeground(Color.WHITE);
        p.add(l, BorderLayout.WEST);

        String displayName = customerName;
        if (displayName == null || displayName.trim().isEmpty()) {
            Customer c = AuthService.getCurrentUser();
            if (c != null) displayName = c.getName();
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Profile";
        }

        JButton profileBtn = createProfileButton(displayName);
        final String finalName = displayName;
        profileBtn.addActionListener(e -> {
            Window parent = (currentFrame != null) ? currentFrame : SwingUtilities.getWindowAncestor(profileBtn);
            if (parent != null) {
                parent.dispose();
            }
            new ProfilePage(finalName).setVisible(true);
        });

        p.add(profileBtn, BorderLayout.EAST);
        return p;
    }

    /**
     * Creates a styled profile pill button with a vector user avatar icon.
     */
    public static JButton createProfileButton(String displayName) {
        String shortName = displayName.contains(" ") ? displayName.split(" ")[0] : displayName;
        JButton btn = new JButton(" " + shortName, new ProfileAvatarIcon(24));
        btn.setFont(new Font(detectSystemSafeFont(), Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(255, 255, 255, 45));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("View Profile Details (" + displayName + ")");

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 160), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255, 80));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255, 45));
            }
        });

        return btn;
    }

    /**
     * Vector-drawn circular user profile icon.
     */
    public static class ProfileAvatarIcon implements Icon {
        private final int size;

        public ProfileAvatarIcon(int size) {
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Circular white badge
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillOval(x, y, size, size);

            // User silhouette in primary color
            g2.setColor(PRIMARY);
            int headSize = (int) (size * 0.38);
            int headX = x + (size - headSize) / 2;
            int headY = y + (int) (size * 0.18);
            g2.fillOval(headX, headY, headSize, headSize);

            int bodyW = (int) (size * 0.72);
            int bodyH = (int) (size * 0.55);
            int bodyX = x + (size - bodyW) / 2;
            int bodyY = y + (int) (size * 0.58);
            g2.fillArc(bodyX, bodyY, bodyW, bodyH, 0, 180);

            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    /**
     * Formats rupee amounts consistently.
     */
    public static String rupees(double amount) {
        return String.format("Rs. %.2f", amount);
    }

    // =========================================================================
    // 6. PRIVATE UTILITIES
    // =========================================================================

    /**
     * Resolves a modern, crisp system-safe font family available on the host OS.
     */
    private static String detectSystemSafeFont() {
        String[] preferredFonts = { "Segoe UI", "Inter", "San Francisco", "Helvetica Neue", "Arial" };
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Set<String> available = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));

        for (String fontName : preferredFonts) {
            if (available.contains(fontName)) {
                return fontName;
            }
        }
        return Font.SANS_SERIF;
    }

    private UIStyle() {
        // Prevent instantiation of utility class
    }
}
