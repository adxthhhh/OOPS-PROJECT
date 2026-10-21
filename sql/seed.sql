-- =============================================================================
-- MealFlow Realistic Seed Data
-- =============================================================================

USE mealplan_db;

-- -----------------------------------------------------------------------------
-- 1. SEED CUSTOMERS
-- Demo passwords are all: password123 (BCrypt cost 10)
-- -----------------------------------------------------------------------------
INSERT INTO customers (customer_id, name, email, phone, password_hash, is_active) VALUES
(1, 'Rahul Sharma', 'rahul@example.com', '9876543210', '$2a$10$DcoQ1d6OrMsiFcvBXz2WsOTdgy0a1yDXUNEXw0ZbpVcB4hB2BAK9m', TRUE),
(2, 'Adithya V', 'adithya@example.com', '9845012345', '$2a$10$DcoQ1d6OrMsiFcvBXz2WsOTdgy0a1yDXUNEXw0ZbpVcB4hB2BAK9m', TRUE),
(3, 'Priya Nair', 'priya@example.com', '9900112233', '$2a$10$DcoQ1d6OrMsiFcvBXz2WsOTdgy0a1yDXUNEXw0ZbpVcB4hB2BAK9m', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- -----------------------------------------------------------------------------
-- 2. SEED MEAL PLANS
-- -----------------------------------------------------------------------------
INSERT INTO meal_plans (plan_id, name, description, plan_type, duration_days, meals_per_day, price, is_active) VALUES
(1, 'Healthy Weekly Cooked', 'Balanced, ready-to-eat meals delivered hot every day.', 'COOKED', 7, 3, 1799.00, TRUE),
(2, 'Monthly Veg Delight', 'Pure vegetarian meals planned for a full month.', 'COOKED', 30, 2, 5499.00, TRUE),
(3, 'Chef''s Weekly Meal Kit', 'Pre-portioned ingredients with step-by-step recipe cards.', 'MEAL_KIT', 7, 1, 1299.00, TRUE),
(4, 'Family Meal Kit (Monthly)', 'Kits for the whole family with fresh ingredients.', 'MEAL_KIT', 30, 2, 6999.00, TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price);

-- -----------------------------------------------------------------------------
-- 3. SEED FOOD ITEMS
-- -----------------------------------------------------------------------------
INSERT INTO food_items (item_id, name, description, meal_type, price, is_veg, is_available) VALUES
(1, 'Veg Poha', 'Flattened rice with peanuts and tempered spices', 'Breakfast', 60.00, TRUE, TRUE),
(2, 'Dal Rice Combo', 'Homestyle dal tadka, steamed basmati rice, kachumber salad', 'Lunch', 120.00, TRUE, TRUE),
(3, 'Grilled Chicken Bowl', 'Herb-marinated chicken breast, quinoa, steamed broccoli', 'Dinner', 180.00, FALSE, TRUE),
(4, 'Idli Sambar', 'Fluffy steamed idlis with lentil sambar and coconut chutney', 'Breakfast', 55.00, TRUE, TRUE),
(5, 'Paneer Thali', 'Paneer butter masala, whole wheat rotis, jeera rice', 'Dinner', 160.00, TRUE, TRUE),
(6, 'Pasta Primavera Kit', 'Artisanal durum wheat pasta, farm-fresh veggies, marinara sauce kit', 'Dinner', 190.00, TRUE, TRUE),
(7, 'Chicken Curry Kit', 'Hand-ground spice blend, tender cut chicken, aromatic basmati kit', 'Dinner', 230.00, FALSE, TRUE),
(8, 'Veg Biryani Kit', 'Aged basmati rice, garden vegetables, shahi biryani masala', 'Lunch', 170.00, TRUE, TRUE),
(9, 'Pancake Kit', 'Wholesome millet pancake mix, raw honey, berry compote', 'Breakfast', 90.00, TRUE, TRUE),
(10, 'Smoked Chicken Multigrain Wrap', 'Charbroiled chicken strips with baby greens in whole wheat wrap', 'Lunch', 220.00, FALSE, TRUE),
(11, 'Paneer Tikka Brown Rice Bowl', 'Spiced cottage cheese, brown rice, edamame, mint yogurt drizzle', 'Lunch', 240.00, TRUE, TRUE),
(12, 'Grilled Tofu & Edamame Quinoa Salad', 'Crispy tofu cubes, tricolor quinoa, avocado dressing', 'Dinner', 260.00, TRUE, TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price);

-- -----------------------------------------------------------------------------
-- 4. SEED MEAL PLAN ITEMS (JUNCTION)
-- -----------------------------------------------------------------------------
INSERT INTO meal_plan_items (plan_id, item_id, quantity) VALUES
(1, 1, 1),
(1, 2, 1),
(1, 3, 1),
(1, 11, 1),
(2, 4, 1),
(2, 5, 1),
(2, 2, 1),
(3, 6, 1),
(3, 7, 1),
(4, 8, 1),
(4, 9, 1),
(4, 6, 1)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- -----------------------------------------------------------------------------
-- 5. SEED SUBSCRIPTIONS
-- -----------------------------------------------------------------------------
INSERT INTO subscriptions (subscription_id, customer_id, plan_id, agreed_price, start_date, end_date, status) VALUES
(1, 1, 1, 1799.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'ACTIVE'),
(2, 2, 2, 5499.00, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'ACTIVE')
ON DUPLICATE KEY UPDATE agreed_price=VALUES(agreed_price);

-- -----------------------------------------------------------------------------
-- 6. SEED SUBSCRIPTION CALENDAR ENTRIES
-- -----------------------------------------------------------------------------
INSERT INTO subscription_calendar (calendar_entry_id, subscription_id, meal_date, meal_type, item_id, custom_notes) VALUES
(1, 1, CURDATE(), 'Breakfast', 1, 'Less spicy, extra lemon'),
(2, 1, CURDATE(), 'Lunch', 11, 'Extra mint chutney'),
(3, 1, CURDATE(), 'Dinner', 3, 'No dressing'),
(4, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'Breakfast', 4, 'Standard prep'),
(5, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'Lunch', 2, 'Mild spice')
ON DUPLICATE KEY UPDATE custom_notes=VALUES(custom_notes);

-- -----------------------------------------------------------------------------
-- 7. SEED ORDERS, ORDER ITEMS, PAYMENTS & STATUS HISTORY
-- -----------------------------------------------------------------------------
INSERT INTO orders (order_id, customer_id, order_status, subtotal, delivery_fee, total_amount, delivery_address, contact_phone, estimated_delivery_time) VALUES
(1001, 1, 'CONFIRMED', 460.00, 0.00, 460.00, 'Flat 402, Green Glen Heights, Indiranagar, Bengaluru', '9876543210', '25-30 mins'),
(1002, 2, 'DELIVERED', 240.00, 0.00, 240.00, 'House 12, Koramangala 4th Block, Bengaluru', '9845012345', 'Delivered at 1:15 PM')
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

INSERT INTO order_items (order_item_id, order_id, item_id, item_name_snapshot, quantity, unit_price_snapshot, subtotal) VALUES
(1, 1001, 11, 'Paneer Tikka Brown Rice Bowl', 1, 240.00, 240.00),
(2, 1001, 10, 'Smoked Chicken Multigrain Wrap', 1, 220.00, 220.00),
(3, 1002, 11, 'Paneer Tikka Brown Rice Bowl', 1, 240.00, 240.00)
ON DUPLICATE KEY UPDATE subtotal=VALUES(subtotal);

INSERT INTO payments (payment_id, order_id, payment_method, amount, payment_status, transaction_ref, payment_details) VALUES
(1, 1001, 'UPI', 460.00, 'SUCCESS', 'TXN-MF-982143', 'Paid via rahul@okaxis'),
(2, 1002, 'CARD', 240.00, 'SUCCESS', 'TXN-MF-551928', 'Paid via Visa ending 3456')
ON DUPLICATE KEY UPDATE amount=VALUES(amount);

INSERT INTO order_status_history (history_id, order_id, old_status, new_status, notes) VALUES
(1, 1001, NULL, 'PLACED', 'Order submitted by customer'),
(2, 1001, 'PLACED', 'CONFIRMED', 'Payment verified via UPI. Kitchen notified.'),
(3, 1002, NULL, 'PLACED', 'Order submitted by customer'),
(4, 1002, 'PLACED', 'CONFIRMED', 'Card payment approved.'),
(5, 1002, 'CONFIRMED', 'PREPARING', 'Chef began meal preparation.'),
(6, 1002, 'PREPARING', 'OUT_FOR_DELIVERY', 'Assigned to delivery executive.'),
(7, 1002, 'OUT_FOR_DELIVERY', 'DELIVERED', 'Delivered at front door with OTP verification.')
ON DUPLICATE KEY UPDATE notes=VALUES(notes);
