package com.foodapp.mealplan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all meal plans (demonstrating Abstraction + Inheritance + Polymorphism).
 */
public abstract class MealPlan {
    private int planId;
    private String name;
    private String description;
    private int durationDays;
    private int mealsPerDay;
    protected double price;
    private boolean active = true;
    private List<FoodItem> foodItems = new ArrayList<>();

    protected MealPlan(int planId, String name, String description,
                       int durationDays, int mealsPerDay, double price) {
        this(planId, name, description, durationDays, mealsPerDay, price, true);
    }

    protected MealPlan(int planId, String name, String description,
                       int durationDays, int mealsPerDay, double price, boolean active) {
        this.planId = planId;
        this.name = name;
        this.description = description;
        this.durationDays = durationDays;
        this.mealsPerDay = mealsPerDay;
        this.price = price;
        this.active = active;
    }

    /** Label shown in the UI, e.g. "Cooked Meals" or "Meal Kit". */
    public abstract String getPlanType();

    /** Polymorphic behaviour: what happens when this plan is prepared. */
    public abstract String prepareMeal();

    public void addFoodItem(FoodItem item)       { foodItems.add(item); }
    public void setFoodItems(List<FoodItem> list) { this.foodItems = (list != null) ? list : new ArrayList<>(); }

    public int getPlanId()            { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }

    public String getName()           { return name; }
    public void setName(String name)  { this.name = name; }

    public String getDescription()    { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationDays()      { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public int getMealsPerDay()       { return mealsPerDay; }
    public void setMealsPerDay(int mealsPerDay) { this.mealsPerDay = mealsPerDay; }

    public double getPrice()          { return price; }
    public void setPrice(double price){ this.price = price; }

    public boolean isActive()         { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<FoodItem> getFoodItems() { return foodItems; }
}
