package com.foodapp.mealplan.dao;

import com.foodapp.mealplan.model.FoodItem;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDAO {

    public List<FoodItem> getItemsByPlan(int planId) throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT item_id, name, description, meal_type, price, is_veg "
                   + "FROM food_items WHERE plan_id = ? ORDER BY item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new FoodItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("meal_type"),
                        rs.getDouble("price"),
                        rs.getBoolean("is_veg")));
                }
            }
        }
        return items;
    }
}
