package com.foodapp.mealplan.model;

/** Ready-to-eat meals delivered hot. */
public class CookedMealPlan extends MealPlan {

    public CookedMealPlan(int planId, String name, String description,
                          int durationDays, int mealsPerDay, double price) {
        super(planId, name, description, durationDays, mealsPerDay, price);
    }

    @Override
    public String getPlanType() { return "Cooked Meals"; }

    @Override
    public String prepareMeal() { return "Preparing ready-to-eat meal"; }
}
