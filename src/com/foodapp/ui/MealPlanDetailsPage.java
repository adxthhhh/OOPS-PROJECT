package com.foodapp.ui;

import com.foodapp.mealplan.dao.MealPlanDAO;
import com.foodapp.mealplan.model.FoodItem;
import com.foodapp.mealplan.model.MealPlan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MealPlanDetailsPage extends JFrame {

    public MealPlanDetailsPage(String customerName, int planId) {
        MealPlan plan = new MealPlanDAO().getPlanById(planId);

        setTitle("Meal Plan Details");
        setSize(850, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        if (plan == null) {
            add(new JLabel("Meal plan not found.", SwingConstants.CENTER));
            return;
        }

        add(UIStyle.header(plan.getName()), BorderLayout.NORTH);

        // ---- Plan summary ----
        JLabel summary = new JLabel("<html><p style='width:700px'>" + plan.getDescription() + "</p><br>"
                + "<b>Type:</b> " + plan.getPlanType()
                + " &nbsp;|&nbsp; <b>Duration:</b> " + plan.getDurationDays() + " days"
                + " &nbsp;|&nbsp; <b>Meals/day:</b> " + plan.getMealsPerDay() + "<br><br>"
                + "<b>Price:</b> " + UIStyle.rupees(plan.getPrice()) + "<br>"
                + "<i>" + plan.prepareMeal() + "</i></html>");
        summary.setFont(UIStyle.BODY);
        summary.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        // ---- Food items table ----
        String[] cols = {"Meal", "Item", "Description", "Veg", "Price"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (FoodItem f : plan.getFoodItems()) {
            model.addRow(new Object[]{f.getMealType(), f.getName(), f.getDescription(),
                    f.isVegetarian() ? "Veg" : "Non-Veg", UIStyle.rupees(f.getPrice())});
        }
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(UIStyle.BODY);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Included Food Items"));
        tablePanel.add(new JScrollPane(table));

        JPanel center = new JPanel(new BorderLayout());
        center.add(summary, BorderLayout.NORTH);
        center.add(tablePanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ---- Buttons ----
        JButton back = new JButton("< Back to Plans");
        back.addActionListener(e -> {
            new MealPlansPage(customerName).setVisible(true);
            dispose();
        });

        JButton select = UIStyle.button("Select & Customize");
        select.addActionListener(e -> {
            new MealCustomizationPage(customerName, plan).setVisible(true);
            dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttons.add(back);
        buttons.add(select);
        add(buttons, BorderLayout.SOUTH);
    }
}
