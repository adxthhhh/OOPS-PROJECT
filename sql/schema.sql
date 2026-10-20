-- Tables for the food delivery app (Person 2 + Person 5 shared schema)
CREATE DATABASE IF NOT EXISTS mealplan_db;
USE mealplan_db;

CREATE TABLE IF NOT EXISTS meal_plans (
    plan_id       INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(255),
    plan_type     ENUM('COOKED','MEAL_KIT') NOT NULL,
    duration_days INT NOT NULL,
    meals_per_day INT NOT NULL,
    price         DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS food_items (
    item_id     INT PRIMARY KEY AUTO_INCREMENT,
    plan_id     INT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    meal_type   ENUM('Breakfast','Lunch','Dinner') NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    is_veg      BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (plan_id) REFERENCES meal_plans(plan_id) ON DELETE CASCADE
);

INSERT INTO meal_plans (name, description, plan_type, duration_days, meals_per_day, price) VALUES
('Healthy Weekly Cooked','Balanced, ready-to-eat meals delivered hot every day.','COOKED',7,3,1799),
('Monthly Veg Delight','Pure vegetarian meals planned for a full month.','COOKED',30,2,5499),
('Chef''s Weekly Meal Kit','Pre-portioned ingredients with step-by-step recipe cards.','MEAL_KIT',7,1,1299),
('Family Meal Kit (Monthly)','Kits for the whole family with fresh ingredients.','MEAL_KIT',30,2,6999);

INSERT INTO food_items (plan_id, name, description, meal_type, price, is_veg) VALUES
(1,'Veg Poha','Flattened rice with peanuts','Breakfast',60,TRUE),
(1,'Dal Rice Combo','Dal tadka, rice, salad','Lunch',120,TRUE),
(1,'Grilled Chicken Bowl','Chicken, quinoa, veggies','Dinner',180,FALSE),
(2,'Idli Sambar','Steamed idli with sambar','Breakfast',55,TRUE),
(2,'Paneer Thali','Paneer curry, roti, rice','Dinner',160,TRUE),
(3,'Pasta Primavera Kit','Pasta, veggies, sauce base','Dinner',190,TRUE),
(3,'Chicken Curry Kit','Spice mix, chicken, rice','Dinner',230,FALSE),
(4,'Veg Biryani Kit','Basmati, veggies, masala','Lunch',170,TRUE),
(4,'Pancake Kit','Batter mix, toppings','Breakfast',90,TRUE);
