package com.foodapp.ui;

import com.foodapp.mealplan.model.FoodItem;
import com.foodapp.mealplan.model.MealPlan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * MealCustomizationPage — Person 3's deliverable.
 * Lets the customer toggle food items in/out of their selected plan,
 * adjust per-item quantity (1–5 servings), filter by diet preference,
 * enter delivery notes, view a live price summary, and proceed to PaymentPage.
 *
 * OOP concepts:
 *  - Encapsulation: inner ItemRow class bundles per-item UI state
 *  - Polymorphism: accepts any MealPlan subclass (CookedMealPlan / MealKitPlan)
 *  - Composition: builds UI from reusable ItemRow and helper methods
 *  - Event-driven: listeners refresh the summary panel in real time
 */
public class MealCustomizationPage extends JFrame {

    // ----- Shared colour palette from ExplorePage -----
    private static final Color C_BG     = ExplorePage.COLOR_BG;
    private static final Color C_PRI    = ExplorePage.COLOR_PRIMARY;
    private static final Color C_SEC    = ExplorePage.COLOR_SECONDARY;
    private static final Color C_SURF   = ExplorePage.COLOR_SURFACE;
    private static final Color C_SLOW   = ExplorePage.COLOR_SURFACE_LOW;
    private static final Color C_SHIGH  = ExplorePage.COLOR_SURFACE_HIGH;
    private static final Color C_TEXT   = ExplorePage.COLOR_TEXT_ON_SURFACE;
    private static final Color C_MUTED  = ExplorePage.COLOR_TEXT_MUTED;
    private static final Color C_OUTL   = ExplorePage.COLOR_OUTLINE_VARIANT;

    private static final Font F_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font F_BODY  = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font F_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final Font F_BOLD  = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final Font F_MONO  = new Font(Font.MONOSPACED, Font.BOLD, 14);

    // ----- State -----
    private final String       customerName;
    private final MealPlan     plan;
    private final List<ItemRow> rows = new ArrayList<>();
    private String dietFilter = "All";

    // Live-update widgets
    private JLabel  planBaseLabel;
    private JLabel  selectedCountLabel;
    private JLabel  extraCostLabel;
    private JLabel  totalLabel;
    private JButton proceedBtn;
    private JTextArea notesArea;
    private JPanel  itemsContainer;

    // =========================================================================
    public MealCustomizationPage(String customerName, MealPlan plan) {
        this.customerName = (customerName != null && !customerName.isBlank()) ? customerName : "Customer";
        this.plan = plan;
        for (FoodItem item : plan.getFoodItems()) rows.add(new ItemRow(item));

        setTitle("MealFlow — Customize Your Plan");
        setSize(1000, 720);
        setMinimumSize(new Dimension(820, 580));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        refreshSummary();
    }

    // =========================================================================
    //  HEADER
    // =========================================================================
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(C_PRI);
        h.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        JLabel title = new JLabel("Customize Your Meal Plan");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel(plan.getName() + "  ·  " + plan.getPlanType()
                + "  ·  " + plan.getDurationDays() + " days");
        sub.setFont(F_SMALL);
        sub.setForeground(new Color(255, 220, 210));
        titles.add(title); titles.add(sub);
        h.add(titles, BorderLayout.WEST);

        JButton back = ghostBtn("← Back to Plan Details");
        back.addActionListener(e -> { new MealPlanDetailsPage(customerName, plan.getPlanId()).setVisible(true); dispose(); });
        h.add(back, BorderLayout.EAST);
        return h;
    }

    // =========================================================================
    //  BODY (split pane)
    // =========================================================================
    private JSplitPane buildBody() {
        // LEFT — filter bar + scrollable item list
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(C_BG);
        left.add(buildFilterBar(), BorderLayout.NORTH);

        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(C_BG);
        itemsContainer.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 8));
        rebuildItems();

        JScrollPane scroll = new JScrollPane(itemsContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(C_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        left.add(scroll, BorderLayout.CENTER);

        // RIGHT — order summary + notes
        JPanel right = buildSidePanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(640);
        split.setDividerSize(3);
        split.setBorder(null);
        split.setResizeWeight(0.68);
        return split;
    }

    // =========================================================================
    //  FILTER BAR
    // =========================================================================
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(C_SLOW);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_OUTL));

        JLabel lbl = new JLabel("Filter:");
        lbl.setFont(F_BOLD);
        lbl.setForeground(C_TEXT);
        bar.add(lbl);

        ButtonGroup bg = new ButtonGroup();
        for (String opt : new String[]{"All", "Veg", "Non-Veg"}) {
            JToggleButton chip = chip(opt, opt.equals(dietFilter));
            bg.add(chip);
            chip.addActionListener(e -> {
                dietFilter = opt;
                rebuildItems();
                refreshSummary();
            });
            bar.add(chip);
        }

        bar.add(Box.createHorizontalStrut(12));
        JButton selAll   = actionBtn("✓ Select All");
        JButton deselAll = actionBtn("✗ Clear All");
        selAll.addActionListener(e  -> { rows.forEach(r -> r.chk.setSelected(true));  refreshSummary(); });
        deselAll.addActionListener(e-> { rows.forEach(r -> r.chk.setSelected(false)); refreshSummary(); });
        bar.add(selAll); bar.add(deselAll);
        return bar;
    }

    // =========================================================================
    //  ITEM LIST
    // =========================================================================
    private void rebuildItems() {
        itemsContainer.removeAll();
        for (ItemRow row : rows) {
            boolean show = switch (dietFilter) {
                case "Veg"     -> row.item.isVegetarian();
                case "Non-Veg" -> !row.item.isVegetarian();
                default        -> true;
            };
            if (show) {
                itemsContainer.add(row.buildCard());
                itemsContainer.add(Box.createVerticalStrut(10));
            }
        }
        if (itemsContainer.getComponentCount() == 0) {
            JLabel empty = new JLabel("No items match this filter.", SwingConstants.CENTER);
            empty.setFont(F_BODY); empty.setForeground(C_MUTED);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            itemsContainer.add(Box.createVerticalStrut(40));
            itemsContainer.add(empty);
        }
        itemsContainer.revalidate();
        itemsContainer.repaint();
    }

    // =========================================================================
    //  SIDE PANEL
    // =========================================================================
    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(C_BG);
        side.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 16));

        // --- Order Summary ---
        JPanel sum = sideCard();
        JLabel sumTitle = new JLabel("Order Summary");
        sumTitle.setFont(F_TITLE); sumTitle.setForeground(C_TEXT);
        sumTitle.setAlignmentX(LEFT_ALIGNMENT);
        sum.add(sumTitle); sum.add(Box.createVerticalStrut(12));

        planBaseLabel      = infoLabel("Plan Base Price:  ₹" + (int) plan.getPrice());
        selectedCountLabel = infoLabel("Items Selected:  —");
        extraCostLabel     = infoLabel("Extra Qty Cost:  ₹0");
        sum.add(planBaseLabel);  sum.add(Box.createVerticalStrut(5));
        sum.add(selectedCountLabel); sum.add(Box.createVerticalStrut(5));
        sum.add(extraCostLabel); sum.add(Box.createVerticalStrut(10));

        JPanel divLine = new JPanel(); divLine.setBackground(C_OUTL);
        divLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divLine.setAlignmentX(LEFT_ALIGNMENT);
        sum.add(divLine); sum.add(Box.createVerticalStrut(10));

        totalLabel = new JLabel("Total: ₹" + (int) plan.getPrice());
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        totalLabel.setForeground(C_PRI); totalLabel.setAlignmentX(LEFT_ALIGNMENT);
        sum.add(totalLabel); sum.add(Box.createVerticalStrut(6));

        JLabel freeDelivery = new JLabel("Free delivery on orders above ₹300");
        freeDelivery.setFont(F_SMALL); freeDelivery.setForeground(C_SEC);
        freeDelivery.setAlignmentX(LEFT_ALIGNMENT);
        sum.add(freeDelivery);
        side.add(sum); side.add(Box.createVerticalStrut(14));

        // --- Plan Info ---
        JPanel info = sideCard();
        JLabel infoTitle = new JLabel("Plan Details");
        infoTitle.setFont(F_BOLD); infoTitle.setForeground(C_TEXT);
        infoTitle.setAlignmentX(LEFT_ALIGNMENT);
        info.add(infoTitle); info.add(Box.createVerticalStrut(8));
        for (String ln : new String[]{
                "Type: " + plan.getPlanType(),
                "Duration: " + plan.getDurationDays() + " days",
                "Meals/day: " + plan.getMealsPerDay(),
                "Preparation: " + plan.prepareMeal()
        }) {
            JLabel l = new JLabel(ln); l.setFont(F_SMALL); l.setForeground(C_MUTED);
            l.setAlignmentX(LEFT_ALIGNMENT);
            info.add(l); info.add(Box.createVerticalStrut(3));
        }
        side.add(info); side.add(Box.createVerticalStrut(14));

        // --- Special Instructions ---
        JPanel notesCard = sideCard();
        JLabel notesTitle = new JLabel("Special Instructions");
        notesTitle.setFont(F_BOLD); notesTitle.setForeground(C_TEXT);
        notesTitle.setAlignmentX(LEFT_ALIGNMENT);
        notesCard.add(notesTitle); notesCard.add(Box.createVerticalStrut(6));

        notesArea = new JTextArea(4, 20);
        notesArea.setFont(F_BODY); notesArea.setLineWrap(true); notesArea.setWrapStyleWord(true);
        notesArea.setBackground(C_BG); notesArea.setForeground(C_TEXT);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JScrollPane ns = new JScrollPane(notesArea);
        ns.setBorder(null); ns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        ns.setAlignmentX(LEFT_ALIGNMENT);
        notesCard.add(ns);
        side.add(notesCard); side.add(Box.createVerticalGlue());
        side.add(Box.createVerticalStrut(14));

        // --- Proceed button ---
        proceedBtn = new JButton("Proceed to Payment →");
        proceedBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        proceedBtn.setBackground(C_PRI); proceedBtn.setForeground(Color.WHITE);
        proceedBtn.setFocusPainted(false); proceedBtn.setBorderPainted(false);
        proceedBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        proceedBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        proceedBtn.setAlignmentX(LEFT_ALIGNMENT);
        proceedBtn.addActionListener(e -> proceedToPayment());
        proceedBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { proceedBtn.setBackground(ExplorePage.COLOR_PRIMARY_CONTAINER); }
            public void mouseExited(MouseEvent e)  { proceedBtn.setBackground(C_PRI); }
        });
        side.add(proceedBtn);
        return side;
    }

    // =========================================================================
    //  FOOTER
    // =========================================================================
    private JPanel buildFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        f.setBackground(C_SLOW);
        f.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_OUTL));
        JLabel hint = new JLabel("Tip: Use the checkbox to include/exclude items. Adjust the quantity spinner for extra servings.");
        hint.setFont(F_SMALL); hint.setForeground(C_MUTED);
        f.add(hint);
        return f;
    }

    // =========================================================================
    //  LIVE SUMMARY REFRESH
    // =========================================================================
    void refreshSummary() {
        int count = 0;
        double extra = 0.0;
        for (ItemRow r : rows) {
            if (r.chk.isSelected()) {
                count++;
                int qty = (int) r.spinner.getValue();
                if (qty > 1) extra += r.item.getPrice() * (qty - 1);
            }
        }
        double total = plan.getPrice() + extra;
        planBaseLabel.setText("Plan Base Price:  ₹" + (int) plan.getPrice());
        selectedCountLabel.setText("Items Selected:  " + count + " / " + plan.getFoodItems().size());
        extraCostLabel.setText("Extra Qty Cost:  " + (extra > 0 ? "₹" + (int) extra : "₹0"));
        totalLabel.setText("Total: ₹" + (int) total);

        boolean ok = count > 0;
        proceedBtn.setEnabled(ok);
        proceedBtn.setBackground(ok ? C_PRI : C_SHIGH);
        proceedBtn.setForeground(ok ? Color.WHITE : C_MUTED);
    }

    // =========================================================================
    //  CHECKOUT FLOW
    // =========================================================================
    private void proceedToPayment() {
        List<String> chosen = new ArrayList<>();
        double extra = 0.0;
        for (ItemRow r : rows) {
            if (r.chk.isSelected()) {
                int qty = (int) r.spinner.getValue();
                chosen.add("• " + r.item.getName() + " × " + qty
                        + (qty > 1 ? "  (+₹" + (int)(r.item.getPrice() * (qty - 1)) + ")" : ""));
                if (qty > 1) extra += r.item.getPrice() * (qty - 1);
            }
        }
        if (chosen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one item.", "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double total = plan.getPrice() + extra;
        String notes = notesArea.getText().trim();

        StringBuilder msg = new StringBuilder("<html><body style='width:340px;font-family:sans-serif;'>");
        msg.append("<h3 style='color:#ab2e16;'>Confirm Customized Order</h3>");
        msg.append("<b>Plan:</b> ").append(plan.getName()).append("<br><b>Items:</b><br>");
        chosen.forEach(c -> msg.append(c).append("<br>"));
        if (!notes.isEmpty()) msg.append("<br><b>Notes:</b> ").append(notes).append("<br>");
        msg.append("<br><b>Total Payable: ₹").append((int) total).append("</b></body></html>");

        int res = JOptionPane.showConfirmDialog(this, new JLabel(msg.toString()),
                "Proceed to Payment?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            new PaymentPage(total).setVisible(true);
            dispose();
        }
    }

    // =========================================================================
    //  INNER CLASS — ItemRow (Encapsulation)
    // =========================================================================
    /**
     * Encapsulates the UI state for a single food item: a checkbox (include/exclude)
     * and a quantity spinner (1–5 servings). Builds its own card panel on demand.
     */
    private class ItemRow {
        final FoodItem  item;
        final JCheckBox chk;
        final JSpinner  spinner;

        ItemRow(FoodItem item) {
            this.item = item;
            this.chk  = new JCheckBox();
            chk.setSelected(true);
            chk.setOpaque(false);
            chk.addActionListener(e -> refreshSummary());

            this.spinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
            spinner.setFont(F_MONO);
            spinner.setPreferredSize(new Dimension(64, 30));
            spinner.addChangeListener(e -> refreshSummary());
        }

        JPanel buildCard() {
            JPanel card = new JPanel(new BorderLayout(12, 0));
            card.setBackground(C_SURF);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            card.setMinimumSize(new Dimension(200, 74));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_OUTL, 1, true),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));

            // Dim card when unchecked
            chk.addActionListener(e -> {
                card.setBackground(chk.isSelected() ? C_SURF : C_SLOW);
                card.repaint();
            });

            // LEFT: checkbox
            JPanel checkWrap = new JPanel(new GridBagLayout());
            checkWrap.setOpaque(false);
            checkWrap.add(chk);
            card.add(checkWrap, BorderLayout.WEST);

            // CENTER: item name, description, meta
            JPanel info = new JPanel(new GridLayout(3, 1, 0, 2));
            info.setOpaque(false);

            JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            nameRow.setOpaque(false);
            JLabel nameLbl = new JLabel(item.getName());
            nameLbl.setFont(F_BOLD); nameLbl.setForeground(C_TEXT);
            JLabel vegBadge = new JLabel(item.isVegetarian() ? " 🟢 Veg" : " 🔴 Non-Veg");
            vegBadge.setFont(F_SMALL);
            vegBadge.setForeground(item.isVegetarian() ? C_SEC : C_PRI);
            nameRow.add(nameLbl); nameRow.add(vegBadge);
            info.add(nameRow);

            String desc = item.getDescription();
            if (desc != null && desc.length() > 70) desc = desc.substring(0, 67) + "...";
            JLabel descLbl = new JLabel(desc);
            descLbl.setFont(F_SMALL); descLbl.setForeground(C_MUTED);
            info.add(descLbl);

            JLabel meta = new JLabel(item.getMealType() + "  •  ₹" + (int) item.getPrice() + " per serving");
            meta.setFont(F_SMALL); meta.setForeground(C_MUTED);
            info.add(meta);

            card.add(info, BorderLayout.CENTER);

            // RIGHT: qty spinner
            JPanel right = new JPanel(new GridBagLayout());
            right.setOpaque(false);
            JPanel qtyRow = new JPanel(new BorderLayout(4, 0));
            qtyRow.setOpaque(false);
            JLabel qtyLbl = new JLabel("Qty:"); qtyLbl.setFont(F_SMALL); qtyLbl.setForeground(C_MUTED);
            qtyRow.add(qtyLbl, BorderLayout.WEST);
            qtyRow.add(spinner, BorderLayout.CENTER);
            right.add(qtyRow);
            card.add(right, BorderLayout.EAST);

            return card;
        }
    }

    // =========================================================================
    //  HELPER UI METHODS
    // =========================================================================
    private JPanel sideCard() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_SURF);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return p;
    }

    private JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_BODY); l.setForeground(C_TEXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JButton ghostBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BOLD); b.setForeground(Color.WHITE);
        b.setBackground(new Color(255, 255, 255, 30));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(new Color(255, 220, 210)); }
            public void mouseExited(MouseEvent e)  { b.setForeground(Color.WHITE); }
        });
        return b;
    }

    private JToggleButton chip(String text, boolean selected) {
        JToggleButton tb = new JToggleButton(text, selected);
        tb.setFont(F_SMALL); tb.setFocusPainted(false);
        tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tb.setBackground(selected ? C_PRI : C_SURF);
        tb.setForeground(selected ? Color.WHITE : C_TEXT);
        tb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        tb.addChangeListener(e -> {
            tb.setBackground(tb.isSelected() ? C_PRI : C_SURF);
            tb.setForeground(tb.isSelected() ? Color.WHITE : C_TEXT);
        });
        return tb;
    }

    private JButton actionBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_SMALL); b.setFocusPainted(false);
        b.setBackground(C_SLOW); b.setForeground(C_TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_OUTL, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
