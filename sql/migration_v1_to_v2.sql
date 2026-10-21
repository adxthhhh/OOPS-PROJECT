-- =============================================================================
-- Migration Script: v1 (Initial 2-table schema) to v2 (10-table normalized schema)
-- Database: mealplan_db
-- =============================================================================

USE mealplan_db;

-- Step 1: Create backup table of original food_items before modifying
CREATE TABLE IF NOT EXISTS food_items_backup_v1 AS SELECT * FROM food_items;

-- Step 2: Create new junction table meal_plan_items if not present
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

-- Step 3: Migrate existing plan-item associations from food_items into meal_plan_items
-- Checks if plan_id column still exists in food_items
SET @has_plan_id = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'food_items'
      AND COLUMN_NAME = 'plan_id'
);

SET @migrate_sql = IF(@has_plan_id > 0,
    'INSERT IGNORE INTO meal_plan_items (plan_id, item_id, quantity) SELECT plan_id, item_id, 1 FROM food_items WHERE plan_id IS NOT NULL',
    'SELECT "Column plan_id already migrated."'
);

PREPARE stmt FROM @migrate_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: Add missing columns to existing food_items table if needed
SET @has_available = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'food_items'
      AND COLUMN_NAME = 'is_available'
);

SET @add_available_sql = IF(@has_available = 0,
    'ALTER TABLE food_items ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT TRUE AFTER is_veg',
    'SELECT "is_available already present in food_items."'
);

PREPARE stmt FROM @add_available_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 5: Safely drop old foreign key and plan_id column from food_items
SET @fk_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'food_items'
      AND COLUMN_NAME = 'plan_id'
      AND REFERENCED_TABLE_NAME = 'meal_plans'
    LIMIT 1
);

SET @drop_fk_sql = IF(@fk_name IS NOT NULL,
    CONCAT('ALTER TABLE food_items DROP FOREIGN KEY ', @fk_name),
    'SELECT "No foreign key on plan_id to drop."'
);

PREPARE stmt FROM @drop_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 6: Drop column plan_id from food_items now that junction table is active
SET @drop_col_sql = IF(@has_plan_id > 0,
    'ALTER TABLE food_items DROP COLUMN plan_id',
    'SELECT "plan_id already dropped."'
);

PREPARE stmt FROM @drop_col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 7: Create all new tables if they do not exist
-- (Customers, Orders, Order Items, Payments, Subscriptions, Calendar, History)

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
