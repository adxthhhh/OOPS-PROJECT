-- =============================================================================
-- MealFlow Database Schema (Production & College Project Baseline)
-- Database Engine: MySQL 8.x / MariaDB (InnoDB, utf8mb4)
-- Covers: Auth, Catalogue, Junction Items, Orders, Payments, Subscriptions, History
-- =============================================================================

CREATE DATABASE IF NOT EXISTS mealplan_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE mealplan_db;

-- -----------------------------------------------------------------------------
-- 1. CUSTOMERS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id   INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone         VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customers_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. MEAL PLANS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meal_plans (
    plan_id       INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(255),
    plan_type     ENUM('COOKED','MEAL_KIT') NOT NULL,
    duration_days INT NOT NULL,
    meals_per_day INT NOT NULL,
    price         DECIMAL(10,2) NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_meal_plans_type (plan_type),
    INDEX idx_meal_plans_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. FOOD ITEMS TABLE
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food_items (
    item_id      INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    meal_type    ENUM('Breakfast','Lunch','Dinner') NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    is_veg       BOOLEAN NOT NULL DEFAULT TRUE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_food_items_meal_type (meal_type),
    INDEX idx_food_items_veg (is_veg)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. MEAL PLAN ITEMS (JUNCTION TABLE)
-- Many-to-many relationship allowing dishes to be reused across plans.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meal_plan_items (
    plan_id    INT NOT NULL,
    item_id    INT NOT NULL,
    quantity   INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (plan_id, item_id),
    FOREIGN KEY (plan_id) REFERENCES meal_plans(plan_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES food_items(item_id) ON DELETE CASCADE,
    INDEX idx_mpi_plan (plan_id),
    INDEX idx_mpi_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. ORDERS TABLE
-- Snapshots delivery address and stores server-authoritative totals.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    order_id                INT PRIMARY KEY AUTO_INCREMENT,
    customer_id             INT NOT NULL,
    order_status            ENUM('PLACED','CONFIRMED','PREPARING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    subtotal                DECIMAL(10,2) NOT NULL,
    delivery_fee            DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount            DECIMAL(10,2) NOT NULL,
    delivery_address        VARCHAR(255) NOT NULL,
    contact_phone           VARCHAR(20) NOT NULL,
    estimated_delivery_time VARCHAR(50),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    INDEX idx_orders_customer (customer_id),
    INDEX idx_orders_status (order_status),
    INDEX idx_orders_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. ORDER ITEMS TABLE
-- Stores immutable price and name snapshots at moment of purchase.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id       INT PRIMARY KEY AUTO_INCREMENT,
    order_id            INT NOT NULL,
    item_id             INT NOT NULL,
    item_name_snapshot  VARCHAR(100) NOT NULL,
    quantity            INT NOT NULL,
    unit_price_snapshot DECIMAL(10,2) NOT NULL,
    subtotal            DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES food_items(item_id) ON DELETE RESTRICT,
    INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. PAYMENTS TABLE
-- Records payment method, status, amount, and reference without storing raw secrets.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    payment_id      INT PRIMARY KEY AUTO_INCREMENT,
    order_id        INT NOT NULL,
    payment_method  ENUM('UPI','CARD','WALLET','COD') NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    payment_status  ENUM('PENDING','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(100),
    payment_details VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_payments_order (order_id),
    INDEX idx_payments_status (payment_status),
    INDEX idx_payments_txn (transaction_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. SUBSCRIPTIONS TABLE
-- Tracks long-term weekly/monthly meal plan subscriptions.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id     INT NOT NULL,
    plan_id         INT NOT NULL,
    agreed_price    DECIMAL(10,2) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    status          ENUM('ACTIVE','PAUSED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    FOREIGN KEY (plan_id) REFERENCES meal_plans(plan_id) ON DELETE RESTRICT,
    INDEX idx_subscriptions_customer (customer_id),
    INDEX idx_subscriptions_status (status),
    INDEX idx_subscriptions_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. SUBSCRIPTION CALENDAR TABLE
-- Supports day-by-day and meal-by-meal custom scheduled food selections.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subscription_calendar (
    calendar_entry_id INT PRIMARY KEY AUTO_INCREMENT,
    subscription_id   INT NOT NULL,
    meal_date         DATE NOT NULL,
    meal_type         ENUM('Breakfast','Lunch','Dinner') NOT NULL,
    item_id           INT NOT NULL,
    custom_notes      VARCHAR(255),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES food_items(item_id) ON DELETE RESTRICT,
    UNIQUE KEY uk_sub_date_meal_item (subscription_id, meal_date, meal_type, item_id),
    INDEX idx_calendar_sub_date (subscription_id, meal_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 10. ORDER STATUS HISTORY TABLE
-- Retains audit trail of status lifecycle transitions.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_status_history (
    history_id  INT PRIMARY KEY AUTO_INCREMENT,
    order_id    INT NOT NULL,
    old_status  ENUM('PLACED','CONFIRMED','PREPARING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED'),
    new_status  ENUM('PLACED','CONFIRMED','PREPARING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED') NOT NULL,
    notes       VARCHAR(255),
    changed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_status_history_order (order_id),
    INDEX idx_status_history_changed (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
