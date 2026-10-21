# MealFlow Database & Backend Documentation

## 1. System Requirements
- **Java:** OpenJDK / Oracle JDK 11 or higher (Tested on OpenJDK 17)
- **Database Engine:** MySQL 8.0+ or MariaDB 10.5+
- **JDBC Driver:** `mysql-connector-j-8.4.0.jar` (Included in `lib/`)
- **Password Hashing:** `jbcrypt-0.4.jar` (Included in `lib/`)
- **UI Framework:** Java Swing + FlatLaf 3.5.4 (Included in `lib/`)

---

## 2. Relational Database Schema (`mealplan_db`)

The database consists of 10 normalized tables adhering to 3NF principles:

| # | Table Name | Purpose | Key Constraints |
|---|------------|---------|-----------------|
| 1 | `customers` | Registered users, contact info, salted BCrypt password hashes | Primary Key (`customer_id`), Unique (`email`) |
| 2 | `meal_plans` | Weekly and monthly meal plans (Cooked & DIY Meal Kits) | Primary Key (`plan_id`) |
| 3 | `food_items` | Individual dishes and ingredients catalogue | Primary Key (`item_id`), Check (`meal_type`) |
| 4 | `meal_plan_items` | Many-to-many junction table assigning dishes to plans | Composite PK (`plan_id`, `item_id`), Foreign Keys |
| 5 | `orders` | Customer checkout orders with snapshot totals & addresses | Primary Key (`order_id`), Foreign Key (`customer_id`) |
| 6 | `order_items` | Line items with immutable snapshot price & name at purchase | Primary Key (`order_item_id`), FKs (`order_id`, `item_id`) |
| 7 | `payments` | Audit records for UPI, Card, Wallet, and COD payments | Primary Key (`payment_id`), FK (`order_id`) |
| 8 | `subscriptions` | Active & past weekly/monthly subscriptions | Primary Key (`subscription_id`), FKs (`customer_id`, `plan_id`) |
| 9 | `subscription_calendar` | Day-by-day scheduled meals inside subscription windows | Primary Key (`calendar_entry_id`), Unique slot constraint |
| 10 | `order_status_history` | Audit trail of lifecycle state transitions | Primary Key (`history_id`), FK (`order_id`) |

---

## 3. Database Initialization & Setup

### A. Fresh Installation
Run the initialization script from the terminal:
```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/seed.sql
```

### B. Upgrading from v1 Schema (Migration)
If an existing installation contains the original 2-table schema with `food_items.plan_id`, execute the automated migration script:
```bash
mysql -u root -p < sql/migration_v1_to_v2.sql
```
*Note: The migration script creates an automatic backup (`food_items_backup_v1`), migrates all relationships to `meal_plan_items`, and safely removes the deprecated foreign key column without resetting auto-increment IDs.*

---

## 4. Configuration & Environment Variables

`DBConnection.java` resolves database credentials using the following hierarchy:

1. **Environment Variables:**
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/mealplan_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   export DB_USER="root"
   export DB_PASSWORD="your_actual_password"
   ```

2. **Optional `db.properties` File (Project Root):**
   ```properties
   db.url=jdbc:mysql://localhost:3306/mealplan_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=your_actual_password
   ```

3. **Safe Default Fallback:**
   Defaults to `root` with no password on `localhost:3306`.

---

## 5. Offline Fallback Policy

- When MySQL is running, all catalogue, user authentication, orders, and payment records are authoritatively persisted in MySQL.
- If MySQL is stopped or unreachable, `DBConnection.isFallbackAllowed()` permits read operations to fall back to `SampleData` and pre-configured demonstration accounts (`rahul@example.com`, `adithya@example.com` / `password123`) so the desktop application UI remains completely demoable for academic vivas without an active MySQL server.
- The fallback policy can be toggled via `DBConnection.setFallbackAllowed(false)` during testing.

---

## 6. How to Compile & Run

### Step 1: Compile all source code
```bash
javac -cp "lib/*:." -d out $(find src -name "*.java")
```

### Step 2: Run Application Entry Point
```bash
# Launch from Login Screen
java -cp "lib/*:out" com.foodapp.ui.LoginPage

# Or launch directly into the Explore Dashboard
java -cp "lib/*:out" com.foodapp.ui.ExplorePage
```

### Step 3: Run Automated Backend Tests
```bash
javac -cp "lib/*:out" -d out $(find test -name "*.java")
java -cp "lib/*:out" com.foodapp.test.BackendTestSuite
```
