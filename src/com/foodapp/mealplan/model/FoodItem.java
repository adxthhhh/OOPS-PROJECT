package com.foodapp.mealplan.model;

/** A single dish that belongs to a meal plan. */
public class FoodItem {
    private int itemId;
    private String name;
    private String description;
    private String mealType;   // Breakfast / Lunch / Dinner
    private double price;
    private boolean vegetarian;

    public FoodItem(int itemId, String name, String description,
                    String mealType, double price, boolean vegetarian) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.mealType = mealType;
        this.price = price;
        this.vegetarian = vegetarian;
    }

    public int getItemId()          { return itemId; }
    public String getName()         { return name; }
    public String getDescription()  { return description; }
    public String getMealType()     { return mealType; }
    public double getPrice()        { return price; }
    public boolean isVegetarian()   { return vegetarian; }

    @Override
    public String toString() { return name + " (" + mealType + ")"; }
}
