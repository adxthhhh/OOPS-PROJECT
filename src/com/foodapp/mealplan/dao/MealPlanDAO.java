package com.foodapp.mealplan.dao;

import com.foodapp.mealplan.model.*;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MealPlanDAO {

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();

    /** All plans (without food items - used by the Meal Plans list page). */
    public List<MealPlan> getAllPlans() {
        List<MealPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM meal_plans ORDER BY plan_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) plans.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("DB unavailable, using sample data: " + e.getMessage());
            return SampleData.getPlans();
        }
        return plans;
    }

    /** One plan including its food items (used by the details page). */
    public MealPlan getPlanById(int planId) {
        String sql = "SELECT * FROM meal_plans WHERE plan_id = ?";

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
        } catch (SQLException e) {
            System.err.println("DB unavailable, using sample data: " + e.getMessage());
            return SampleData.getPlanById(planId);
        }
        return null;
    }

    /** Builds the right subclass depending on plan_type (polymorphism). */
    private MealPlan mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("plan_id");
        String name = rs.getString("name");
        String desc = rs.getString("description");
        int days = rs.getInt("duration_days");
        int meals = rs.getInt("meals_per_day");
        double price = rs.getDouble("price");

        return "MEAL_KIT".equals(rs.getString("plan_type"))
                ? new MealKitPlan(id, name, desc, days, meals, price)
                : new CookedMealPlan(id, name, desc, days, meals, price);
    }
}
