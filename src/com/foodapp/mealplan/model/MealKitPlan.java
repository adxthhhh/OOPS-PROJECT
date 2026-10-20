package com.foodapp.mealplan.model;

/** Uncooked, pre-portioned ingredients with a recipe card. */
public class MealKitPlan extends MealPlan {

    public MealKitPlan(int planId, String name, String description,
                       int durationDays, int mealsPerDay, double price) {
        super(planId, name, description, durationDays, mealsPerDay, price);
    }

    @Override
    public String getPlanType() { return "Meal Kit"; }

    @Override
    public String prepareMeal() { return "Packing ingredients and recipe"; }
}
