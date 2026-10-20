package com.foodapp.mealplan.dao;

import com.foodapp.mealplan.model.*;

import java.util.ArrayList;
import java.util.List;

/** Offline fallback so the UI can be demoed without MySQL running. */
class SampleData {

    static List<MealPlan> getPlans() {
        List<MealPlan> list = new ArrayList<>();
        for (int i = 1; i <= 4; i++) list.add(getPlanById(i));
        return list;
    }

    static MealPlan getPlanById(int id) {
        MealPlan p;
        switch (id) {
            case 1:
                p = new CookedMealPlan(1, "Healthy Weekly Cooked",
                    "Balanced, ready-to-eat meals delivered hot every day.", 7, 3, 1799);
                p.addFoodItem(new FoodItem(1, "Veg Poha", "Flattened rice with peanuts", "Breakfast", 60, true));
                p.addFoodItem(new FoodItem(2, "Dal Rice Combo", "Dal tadka, rice, salad", "Lunch", 120, true));
                p.addFoodItem(new FoodItem(3, "Grilled Chicken Bowl", "Chicken, quinoa, veggies", "Dinner", 180, false));
                break;
            case 2:
                p = new CookedMealPlan(2, "Monthly Veg Delight",
                    "Pure vegetarian meals planned for a full month.", 30, 2, 5499);
                p.addFoodItem(new FoodItem(4, "Idli Sambar", "Steamed idli with sambar", "Breakfast", 55, true));
                p.addFoodItem(new FoodItem(5, "Paneer Thali", "Paneer curry, roti, rice", "Dinner", 160, true));
                break;
            case 3:
                p = new MealKitPlan(3, "Chef's Weekly Meal Kit",
                    "Pre-portioned ingredients with step-by-step recipe cards.", 7, 1, 1299);
                p.addFoodItem(new FoodItem(6, "Pasta Primavera Kit", "Pasta, veggies, sauce base", "Dinner", 190, true));
                p.addFoodItem(new FoodItem(7, "Chicken Curry Kit", "Spice mix, chicken, rice", "Dinner", 230, false));
                break;
            default:
                p = new MealKitPlan(4, "Family Meal Kit (Monthly)",
                    "Kits for the whole family with fresh ingredients.", 30, 2, 6999);
                p.addFoodItem(new FoodItem(8, "Veg Biryani Kit", "Basmati, veggies, masala", "Lunch", 170, true));
                p.addFoodItem(new FoodItem(9, "Pancake Kit", "Batter mix, toppings", "Breakfast", 90, true));
        }
        return p;
    }
}
