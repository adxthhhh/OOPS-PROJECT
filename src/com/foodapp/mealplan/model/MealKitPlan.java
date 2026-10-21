package com.foodapp.mealplan.model;

/** DIY ingredient kit with recipe cards. */
public class MealKitPlan extends MealPlan {

    public MealKitPlan(int planId, String name, String description,
                       int durationDays, int mealsPerDay, double price) {
        super(planId, name, description, durationDays, mealsPerDay, price);
    }

    public MealKitPlan(int planId, String name, String description,
                       int durationDays, int mealsPerDay, double price, boolean active) {
        super(planId, name, description, durationDays, mealsPerDay, price, active);
    }

    @Override
    public String getPlanType() { return "Meal Kit"; }

    @Override
    public String prepareMeal() { return "Packing fresh pre-portioned ingredients and recipe card"; }
}
