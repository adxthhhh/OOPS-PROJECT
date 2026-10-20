package com.foodapp.mealplan.model;

import java.util.ArrayList;
import java.util.List;

/** Abstract base class for all meal plans (abstraction + inheritance). */
public abstract class MealPlan {
    private int planId;
    private String name;
    private String description;
    private int durationDays;
    private int mealsPerDay;
    protected double price;
    private List<FoodItem> foodItems = new ArrayList<>();

    protected MealPlan(int planId, String name, String description,
                       int durationDays, int mealsPerDay, double price) {
        this.planId = planId;
        this.name = name;
        this.description = description;
        this.durationDays = durationDays;
        this.mealsPerDay = mealsPerDay;
        this.price = price;
    }

    /** Label shown in the UI, e.g. "Cooked Meals". */
    public abstract String getPlanType();

    /** Polymorphic behaviour: what happens when this plan is prepared. */
    public abstract String prepareMeal();

    public void addFoodItem(FoodItem item)       { foodItems.add(item); }
    public void setFoodItems(List<FoodItem> list) { this.foodItems = list; }

    public int getPlanId()            { return planId; }
    public String getName()           { return name; }
    public String getDescription()    { return description; }
    public int getDurationDays()      { return durationDays; }
    public int getMealsPerDay()       { return mealsPerDay; }
    public double getPrice()          { return price; }
    public List<FoodItem> getFoodItems() { return foodItems; }
}
