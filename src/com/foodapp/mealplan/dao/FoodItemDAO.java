package com.foodapp.mealplan.dao;

import com.foodapp.mealplan.model.FoodItem;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FoodItemDAO - Data Access Object for FoodItem catalogue and meal-plan item associations.
 */
public class FoodItemDAO {

    /**
     * Retrieves all items assigned to a meal plan using the normalized meal_plan_items junction table.
     * Backwards-compatible with v1 schema if migration has not been applied yet.
     */
    public List<FoodItem> getItemsByPlan(int planId) throws SQLException {
        List<FoodItem> items = new ArrayList<>();

        // 1. Primary Query using normalized junction table
        String sql = "SELECT f.item_id, f.name, f.description, f.meal_type, f.price, f.is_veg, f.is_available " +
                     "FROM food_items f " +
                     "JOIN meal_plan_items mpi ON f.item_id = mpi.item_id " +
                     "WHERE mpi.plan_id = ? " +
                     "ORDER BY f.item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            // Check for v1 schema fallback if meal_plan_items junction table does not exist
            if (e.getMessage() != null && e.getMessage().contains("meal_plan_items")) {
                return getItemsByPlanV1Fallback(planId);
            }
            throw e;
        }

        return items;
    }

    private List<FoodItem> getItemsByPlanV1Fallback(int planId) throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT item_id, name, description, meal_type, price, is_veg, TRUE as is_available " +
                     "FROM food_items WHERE plan_id = ? ORDER BY item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return items;
    }

    public List<FoodItem> getAllItems() throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT item_id, name, description, meal_type, price, is_veg, is_available " +
                     "FROM food_items ORDER BY item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(mapRow(rs));
            }
        }
        return items;
    }

    public FoodItem getItemById(int itemId) throws SQLException {
        String sql = "SELECT item_id, name, description, meal_type, price, is_veg, is_available " +
                     "FROM food_items WHERE item_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<FoodItem> filterItems(String mealType, Boolean isVeg) throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT item_id, name, description, meal_type, price, is_veg, is_available FROM food_items WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (mealType != null && !mealType.trim().isEmpty() && !mealType.equalsIgnoreCase("All")) {
            sql.append("AND meal_type = ? ");
            params.add(mealType.trim());
        }
        if (isVeg != null) {
            sql.append("AND is_veg = ? ");
            params.add(isVeg);
        }
        sql.append("ORDER BY item_id");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return items;
    }

    public boolean createItem(FoodItem item) throws SQLException {
        String sql = "INSERT INTO food_items (name, description, meal_type, price, is_veg, is_available) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getMealType());
            ps.setDouble(4, item.getPrice());
            ps.setBoolean(5, item.isVegetarian());
            ps.setBoolean(6, item.isAvailable());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        item.setItemId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    private FoodItem mapRow(ResultSet rs) throws SQLException {
        boolean available = true;
        try {
            available = rs.getBoolean("is_available");
        } catch (SQLException ignored) {
            // In case column is missing in legacy schema
        }

        return new FoodItem(
                rs.getInt("item_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("meal_type"),
                rs.getDouble("price"),
                rs.getBoolean("is_veg"),
                available
        );
    }
}
