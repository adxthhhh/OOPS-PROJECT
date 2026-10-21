package com.foodapp.mealplan.dao;

import com.foodapp.mealplan.model.*;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MealPlanDAO - Data Access Object for MealPlan catalogue and plans in MySQL.
 * Adheres to the documented offline fallback policy: SampleData is only returned if
 * DBConnection.isFallbackAllowed() is true.
 */
public class MealPlanDAO {

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();

    /**
     * Retrieves all active meal plans from MySQL.
     * Attaches associated food items for each plan.
     */
    public List<MealPlan> getAllPlans() {
        List<MealPlan> plans = new ArrayList<>();
        String sql = "SELECT plan_id, name, description, plan_type, duration_days, meals_per_day, price, is_active " +
                     "FROM meal_plans WHERE is_active = TRUE ORDER BY plan_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MealPlan p = mapRow(rs);
                try {
                    p.setFoodItems(foodItemDAO.getItemsByPlan(p.getPlanId()));
                } catch (SQLException ignored) {
                    // Non-fatal if items fail to load on list view
                }
                plans.add(p);
            }
            return plans;

        } catch (SQLException e) {
            if (DBConnection.isFallbackAllowed()) {
                System.err.println("[MealPlanDAO] MySQL connection offline (" + e.getMessage() + "). Falling back to offline SampleData.");
                return SampleData.getPlans();
            } else {
                throw new RuntimeException("Database error in MealPlanDAO.getAllPlans() and fallback is disabled: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Retrieves a single meal plan by ID including its associated food items.
     */
    public MealPlan getPlanById(int planId) {
        String sql = "SELECT plan_id, name, description, plan_type, duration_days, meals_per_day, price, is_active " +
                     "FROM meal_plans WHERE plan_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MealPlan plan = mapRow(rs);
                    plan.setFoodItems(foodItemDAO.getItemsByPlan(planId));
                    return plan;
                }
            }
            return null;

        } catch (SQLException e) {
            if (DBConnection.isFallbackAllowed()) {
                System.err.println("[MealPlanDAO] MySQL connection offline (" + e.getMessage() + "). Falling back to offline SampleData for ID: " + planId);
                return SampleData.getPlanById(planId);
            } else {
                throw new RuntimeException("Database error in MealPlanDAO.getPlanById(" + planId + ") and fallback is disabled: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Persists a new meal plan into the database.
     */
    public boolean createPlan(MealPlan plan) throws SQLException {
        String sql = "INSERT INTO meal_plans (name, description, plan_type, duration_days, meals_per_day, price, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, plan.getName());
            ps.setString(2, plan.getDescription());
            String type = plan instanceof MealKitPlan ? "MEAL_KIT" : "COOKED";
            ps.setString(3, type);
            ps.setInt(4, plan.getDurationDays());
            ps.setInt(5, plan.getMealsPerDay());
            ps.setDouble(6, plan.getPrice());
            ps.setBoolean(7, plan.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        plan.setPlanId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Associates a food item with a meal plan via the junction table.
     */
    public boolean addItemToPlan(int planId, int itemId, int quantity) throws SQLException {
        String sql = "INSERT INTO meal_plan_items (plan_id, item_id, quantity) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, planId);
            ps.setInt(2, itemId);
            ps.setInt(3, quantity > 0 ? quantity : 1);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Removes an item association from a meal plan.
     */
    public boolean removeItemFromPlan(int planId, int itemId) throws SQLException {
        String sql = "DELETE FROM meal_plan_items WHERE plan_id = ? AND item_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, planId);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Factory method demonstrating polymorphism: constructs CookedMealPlan or MealKitPlan based on plan_type column.
     */
    private MealPlan mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("plan_id");
        String name = rs.getString("name");
        String desc = rs.getString("description");
        int days = rs.getInt("duration_days");
        int meals = rs.getInt("meals_per_day");
        double price = rs.getDouble("price");

        boolean active = true;
        try {
            active = rs.getBoolean("is_active");
        } catch (SQLException ignored) {
        }

        String type = rs.getString("plan_type");
        return "MEAL_KIT".equalsIgnoreCase(type)
                ? new MealKitPlan(id, name, desc, days, meals, price, active)
                : new CookedMealPlan(id, name, desc, days, meals, price, active);
    }
}
