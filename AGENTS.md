# AGENTS.md — Food Delivery App (OOPS Project)

> This file tracks the **current development progress** of every module, 
> provides step-by-step instructions to **compile and run** the app, and 
> describes what the user will **see on screen** at each step.
> 
> **Update your section** when you complete a feature or fix a bug.

---

## Overall Progress

| # | Person | Module | Status |
|---|--------|--------|--------|
| 1 | Person 1 | Auth — Login & Register | ✅ Complete |
| 2 | Person 2 | Meal Plans — Browse & Details | ✅ Complete |
| 3 | Person 3 | Meal Customization | ⏳ Not started — TODO hook in `MealPlanDetailsPage` |
| 4 | Person 4 (Adarsh) | Payment Processing | ✅ Complete |
| 5 | Person 5 | My Orders + Real DB Connection | ⏳ Not started — TODO hooks in `HomePage` & `DBConnection` |

---

## Module Status Details

### ✅ Person 1 — Auth Module
**Files:** `src/com/foodapp/auth/`, `src/com/foodapp/ui/LoginPage.java`, `src/com/foodapp/ui/RegisterPage.java`

**What's done:**
- [x] `Customer` POJO (name, email, phone, password)
- [x] `AuthService` — in-memory register/login/logout (List-based store)
- [x] `AuthValidator` — email regex, phone (10 digits), password (min 6 chars), non-empty checks
- [x] `LoginPage` Swing UI — email + password form, validation, navigates to `HomePage` on success
- [x] `RegisterPage` Swing UI — 5-field form with full validation

**Pending:**
- [ ] Replace `AuthService` in-memory store with JDBC calls (Person 5's responsibility)

---

### ✅ Person 2 — Meal Plan Module
**Files:** `src/com/foodapp/mealplan/`, `src/com/foodapp/ui/HomePage.java`, `src/com/foodapp/ui/MealPlansPage.java`, `src/com/foodapp/ui/MealPlanDetailsPage.java`, `src/com/foodapp/ui/UIStyle.java`

**What's done:**
- [x] `MealPlan` abstract class (planId, name, description, duration, mealsPerDay, price, foodItems)
- [x] `CookedMealPlan` — concrete subclass for ready-to-eat plans
- [x] `MealKitPlan` — concrete subclass for DIY ingredient kits
- [x] `FoodItem` — entity with name, description, mealType, price, vegetarian flag
- [x] `MealPlanDAO` — DB queries with automatic fallback to `SampleData` if MySQL is unreachable
- [x] `FoodItemDAO` — fetches food items per plan from DB
- [x] `SampleData` — 4 hardcoded plans (2 Cooked, 2 Meal Kit) for offline demo
- [x] `HomePage` Swing UI — welcome screen with "Browse Meal Plans" and "My Orders" buttons
- [x] `MealPlansPage` Swing UI — grid of plan cards with type filter dropdown
- [x] `MealPlanDetailsPage` Swing UI — plan summary + food items table + "Select & Customize" button
- [x] `UIStyle` — shared theme (green primary, fonts, header/button factories)

**Pending:**
- [ ] `MealPlanDetailsPage` → "Select & Customize" button (Person 3 fills this in)
- [ ] `HomePage` → "My Orders" button (Person 5 fills this in)

---

### ⏳ Person 3 — Meal Customization Module
**Files:** *(not yet created)*

**What's needed:**
- [ ] Create a `MealCustomizationPage` Swing UI
- [ ] Hook into `MealPlanDetailsPage.java` at the marked TODO:
  ```java
  // TODO (Person 3): open the Meal Customization page, passing 'plan'
  select.addActionListener(e -> /* open your page here */);
  ```

---

### ✅ Person 4 (Adarsh) — Payment Module
**Files:** `src/com/foodapp/payment/`, `src/com/foodapp/ui/PaymentPage.java`, `src/com/foodapp/ui/PaymentResultPage.java`

**What's done:**
- [x] `Payment` abstract class — amount, status (PENDING/SUCCESS/FAILED), transactionId, message
- [x] `UPIPayment` — validates UPI ID format (`name@bank`)
- [x] `CardPayment` — validates 16-digit card number, MM/YY expiry, 3-digit CVV
- [x] `WalletPayment` — checks balance sufficiency before deducting
- [x] `CODPayment` — validates delivery address is non-empty
- [x] `PaymentPage` Swing UI — radio-button method selector with `CardLayout` detail panels
- [x] `PaymentResultPage` Swing UI — displays SUCCESS (green) or FAILED (red) with transaction ID
- [x] `PaymentTest` — CLI test runner covering valid + invalid inputs for all 4 methods

**Pending:**
- [ ] `PaymentResultPage` → "Done" button currently does `System.exit(0)`; Person 5 should wire it to "My Orders"

---

### ⏳ Person 5 — Orders & Database Module
**Files:** `src/com/foodapp/util/DBConnection.java` *(placeholder exists)*

**What's needed:**
- [ ] Replace `DBConnection.java` with real MySQL credentials/connection pool
- [ ] Create a `MyOrdersPage` Swing UI
- [ ] Hook into `HomePage.java` at the marked TODO:
  ```java
  // TODO (Person 5): open the My Orders page here
  myOrders.addActionListener(e -> /* open your page here */);
  ```
- [ ] Hook into `PaymentResultPage.java`:
  ```java
  if (ok) System.exit(0);  // TODO: go to My Orders page (Person 5)
  ```
- [ ] Replace `AuthService` in-memory store with JDBC-backed user persistence

---

## How to Compile & Run

### Prerequisites
- Java 11 or higher installed
- Terminal opened at the project root (`OOPS PROJECT/`)

### Step 1 — Compile (no MySQL needed)
```bash
javac -cp "lib/*" -d out $(find src -name "*.java")
```
Expected output: *no errors*, and an `out/` folder is created with all `.class` files.

### Step 2 — Run the App
```bash
java -cp "lib/*:out" com.foodapp.ui.LoginPage
```
The **Login window** will open immediately with modern FlatLaf styling.

### Step 3 — Run Payment Tests (CLI, no UI)
```bash
javac -cp "lib/*:out" -d out $(find src test -name "*.java")
java -cp "lib/*:out" com.foodapp.payment.PaymentTest
```

---

## What You Will See (UI Flow)

```
[LoginPage]
  ├─ Enter email + password → click Login
  │    ├─ Validation errors shown in pop-up dialog
  │    └─ On success → [HomePage]
  │
  └─ Click Register → [RegisterPage]
       ├─ Fill name, email, phone, password, confirm password
       ├─ Validation errors shown in pop-up dialog
       └─ On success → back to [LoginPage]

[HomePage]  (shows "Welcome, <name>!")
  ├─ "Browse Meal Plans" → [MealPlansPage]
  └─ "My Orders" → pop-up "coming from Person 5" (placeholder)

[MealPlansPage]
  ├─ Filter dropdown: All Plans / Cooked Meals / Meal Kit
  ├─ Grid of plan cards (name, description, type, duration, price)
  └─ "View Details" on any card → [MealPlanDetailsPage]

[MealPlanDetailsPage]
  ├─ Plan summary (type, duration, meals/day, price, prepareMeal())
  ├─ Food items table (meal type, name, description, veg/non-veg, price)
  ├─ "< Back to Plans" → [MealPlansPage]
  └─ "Select & Customize" → pop-up "coming from Person 3" (placeholder)
       (Person 3 will replace this with PaymentPage / CustomizationPage)

[PaymentPage]  (launched with an amount)
  ├─ Radio buttons: UPI | Card | Wallet | Cash on Delivery
  ├─ Detail form changes dynamically (CardLayout)
  └─ "Pay Rs.<amount>" → validates input → [PaymentResultPage]

[PaymentResultPage]
  ├─ SUCCESS (green): Method, Amount, Transaction ID, message
  │    └─ "Done" → exits app (Person 5 will wire to My Orders)
  └─ FAILED (red): Method, Amount, failure reason
       └─ "Try Again" → back to [PaymentPage]
```

---

## Running with MySQL (Optional)

1. Start MySQL and run the schema:
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
2. Edit DB credentials in [`src/com/foodapp/util/DBConnection.java`](file:///home/adith/Documents/OOPS%20PROJECT/src/com/foodapp/util/DBConnection.java):
   ```java
   private static final String PASS = "your_actual_password";
   ```
3. Download `mysql-connector-j-*.jar` and place it in the project root.
4. Compile & run with the JAR on the classpath:
   ```bash
   javac -cp mysql-connector-j.jar -d out $(find src -name "*.java")
   java -cp out:mysql-connector-j.jar com.foodapp.ui.LoginPage
   ```

> **Note:** If MySQL is unreachable, the app automatically falls back to `SampleData` 
> for meal plans, so the UI is always fully demo-able without a database.

---

## Key Integration TODOs (Quick Reference)

| File | Line | Owner | What to do |
|------|------|-------|------------|
| [`MealPlanDetailsPage.java`](file:///home/adith/Documents/OOPS%20PROJECT/src/com/foodapp/ui/MealPlanDetailsPage.java) | `select.addActionListener(...)` | Person 3 | Open Customization page |
| [`HomePage.java`](file:///home/adith/Documents/OOPS%20PROJECT/src/com/foodapp/ui/HomePage.java) | `myOrders.addActionListener(...)` | Person 5 | Open My Orders page |
| [`PaymentResultPage.java`](file:///home/adith/Documents/OOPS%20PROJECT/src/com/foodapp/ui/PaymentResultPage.java) | `if (ok) System.exit(0)` | Person 5 | Navigate to My Orders page |
| [`DBConnection.java`](file:///home/adith/Documents/OOPS%20PROJECT/src/com/foodapp/util/DBConnection.java) | `PASS` field | Person 5 | Replace with real DB connection |
| [`AuthService.java`](file:///home/adith/Documents/OOPS%20PROJECT/src/com/foodapp/auth/AuthService.java) | `List<Customer> customers` | Person 5 | Replace with JDBC-backed store |
