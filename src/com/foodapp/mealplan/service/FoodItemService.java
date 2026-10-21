package com.foodapp.mealplan.service;

import com.foodapp.mealplan.dao.FoodItemDAO;
import com.foodapp.mealplan.model.FoodItem;

import java.sql.SQLException;
import java.util.List;

/**
 * FoodItemService - Business logic and catalogue operations for individual food items.
 */
public class FoodItemService {

    private final FoodItemDAO foodItemDAO;

    public FoodItemService() {
        this.foodItemDAO = new FoodItemDAO();
    }

    public FoodItemService(FoodItemDAO foodItemDAO) {
        this.foodItemDAO = foodItemDAO;
    }

    public List<FoodItem> getAllFoodItems() throws SQLException {
        return foodItemDAO.getAllItems();
    }

    public FoodItem getFoodItemById(int itemId) throws SQLException {
        if (itemId <= 0) return null;
        return foodItemDAO.getItemById(itemId);
    }

    public List<FoodItem> getFoodItemsByPlan(int planId) throws SQLException {
        if (planId <= 0) throw new IllegalArgumentException("Invalid planId");
        return foodItemDAO.getItemsByPlan(planId);
    }

    public List<FoodItem> filterFoodItems(String mealType, Boolean isVeg) throws SQLException {
        return foodItemDAO.filterItems(mealType, isVeg);
    }

    public boolean isItemAvailable(int itemId) throws SQLException {
        FoodItem item = getFoodItemById(itemId);
        return item != null && item.isAvailable();
    }

    public boolean createFoodItem(FoodItem item) throws SQLException {
        if (item == null || item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (item.getPrice() <= 0) {
            throw new IllegalArgumentException("Item price must be positive.");
        }
        return foodItemDAO.createItem(item);
    }
}
