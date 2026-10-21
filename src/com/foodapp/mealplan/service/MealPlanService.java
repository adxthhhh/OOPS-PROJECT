package com.foodapp.mealplan.service;

import com.foodapp.mealplan.dao.MealPlanDAO;
import com.foodapp.mealplan.model.MealPlan;

import java.sql.SQLException;
import java.util.List;

/**
 * MealPlanService - Business logic and validation for Meal Plans.
 */
public class MealPlanService {

    private final MealPlanDAO mealPlanDAO;

    public MealPlanService() {
        this.mealPlanDAO = new MealPlanDAO();
    }

    public MealPlanService(MealPlanDAO mealPlanDAO) {
        this.mealPlanDAO = mealPlanDAO;
    }

    public List<MealPlan> getAllActivePlans() {
        return mealPlanDAO.getAllPlans();
    }

    public MealPlan getPlanById(int planId) {
        if (planId <= 0) return null;
        return mealPlanDAO.getPlanById(planId);
    }

    public boolean createMealPlan(MealPlan plan) throws SQLException {
        validatePlan(plan);
        return mealPlanDAO.createPlan(plan);
    }

    public boolean addFoodItemToPlan(int planId, int itemId, int quantity) throws SQLException {
        if (planId <= 0 || itemId <= 0) {
            throw new IllegalArgumentException("Invalid planId or itemId");
        }
        return mealPlanDAO.addItemToPlan(planId, itemId, quantity > 0 ? quantity : 1);
    }

    public boolean removeItemFromPlan(int planId, int itemId) throws SQLException {
        if (planId <= 0 || itemId <= 0) {
            throw new IllegalArgumentException("Invalid planId or itemId");
        }
        return mealPlanDAO.removeItemFromPlan(planId, itemId);
    }

    public void validatePlan(MealPlan plan) {
        if (plan == null) throw new IllegalArgumentException("Meal plan cannot be null.");
        if (plan.getName() == null || plan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Plan name is required.");
        }
        if (plan.getDurationDays() <= 0) {
            throw new IllegalArgumentException("Plan duration must be at least 1 day.");
        }
        if (plan.getMealsPerDay() <= 0) {
            throw new IllegalArgumentException("Meals per day must be at least 1.");
        }
        if (plan.getPrice() <= 0) {
            throw new IllegalArgumentException("Plan price must be greater than zero.");
        }
    }
}
