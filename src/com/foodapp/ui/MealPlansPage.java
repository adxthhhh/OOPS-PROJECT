package com.foodapp.ui;

import com.foodapp.mealplan.dao.MealPlanDAO;
import com.foodapp.mealplan.model.MealPlan;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MealPlansPage extends JFrame {

    private final String customerName;
    private final MealPlanDAO planDAO = new MealPlanDAO();
    private final JPanel cardContainer = new JPanel(new GridLayout(0, 2, 15, 15));
    private final JComboBox<String> filter =
            new JComboBox<>(new String[]{"All Plans", "Cooked Meals", "Meal Kit"});

    public MealPlansPage(String customerName) {
        this.customerName = customerName;

        setTitle("Meal Plans");
        setSize(900, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIStyle.headerWithProfile("Choose Your Meal Plan", this, customerName), BorderLayout.NORTH);

        // Top bar: back + filter
        JButton back = new JButton("< Home");
        back.addActionListener(e -> {
            new HomePage(customerName).setVisible(true);
            dispose();
        });
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        bar.setBackground(UIStyle.BG);
        bar.add(back);
        bar.add(new JLabel("Show:"));
        bar.add(filter);
        filter.addActionListener(e -> loadPlans());

        cardContainer.setBackground(UIStyle.BG);
        cardContainer.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel body = new JPanel(new BorderLayout());
        body.add(bar, BorderLayout.NORTH);
        body.add(new JScrollPane(cardContainer), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        loadPlans();
    }

    private void loadPlans() {
        cardContainer.removeAll();
        String selected = (String) filter.getSelectedItem();
        List<MealPlan> plans = planDAO.getAllPlans();

        for (MealPlan p : plans) {
            if ("All Plans".equals(selected) || p.getPlanType().equals(selected)) {
                cardContainer.add(createCard(p));
            }
        }
        cardContainer.revalidate();
        cardContainer.repaint();
    }

    private JPanel createCard(MealPlan plan) {
        JPanel card = new JPanel(new BorderLayout(5, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCCCCC)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel name = new JLabel(plan.getName());
        name.setFont(UIStyle.HEADING);

        JTextArea desc = new JTextArea(plan.getDescription());
        desc.setFont(UIStyle.BODY);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);

        JLabel info = new JLabel("<html>" + plan.getPlanType() + " | "
                + plan.getDurationDays() + " days | "
                + plan.getMealsPerDay() + " meals/day<br><b>"
                + UIStyle.rupees(plan.getPrice()) + "</b></html>");
        info.setFont(UIStyle.BODY);

        JButton view = UIStyle.button("View Details");
        view.addActionListener(e -> {
            new MealPlanDetailsPage(customerName, plan.getPlanId()).setVisible(true);
            dispose();
        });

        JPanel south = new JPanel(new BorderLayout(5, 8));
        south.setOpaque(false);
        south.add(info, BorderLayout.CENTER);
        south.add(view, BorderLayout.SOUTH);

        card.add(name, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }
}
