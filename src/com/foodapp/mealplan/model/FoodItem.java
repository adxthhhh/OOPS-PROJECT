package com.foodapp.mealplan.model;

/**
 * A single dish that can belong to multiple meal plans or be ordered individually.
 */
public class FoodItem {
    private int itemId;
    private String name;
    private String description;
    private String mealType;   // Breakfast / Lunch / Dinner
    private double price;
    private boolean vegetarian;
    private boolean available = true;

    public FoodItem(int itemId, String name, String description,
                    String mealType, double price, boolean vegetarian) {
        this(itemId, name, description, mealType, price, vegetarian, true);
    }

    public FoodItem(int itemId, String name, String description,
                    String mealType, double price, boolean vegetarian, boolean available) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.mealType = mealType;
        this.price = price;
        this.vegetarian = vegetarian;
        this.available = available;
    }

    public int getItemId()          { return itemId; }
    public void setItemId(int id)   { this.itemId = id; }

    public String getName()         { return name; }
    public void setName(String name){ this.name = name; }

    public String getDescription()  { return description; }
    public void setDescription(String desc) { this.description = desc; }

    public String getMealType()     { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public double getPrice()        { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isVegetarian()   { return vegetarian; }
    public void setVegetarian(boolean veg) { this.vegetarian = veg; }

    public boolean isAvailable()    { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() { return name + " (" + mealType + ") - Rs." + price; }
}
