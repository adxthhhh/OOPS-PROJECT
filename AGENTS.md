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
| 3 | Person 3 | Meal Customization | ✅ Complete |
| 4 | Person 4 (Adarsh) | Payment Processing | ✅ Complete |
| 5 | Person 5 | Orders + Real DB Connection + Subscriptions | ✅ Complete |

---

## Module Status Details

### ✅ Person 1 — Auth Module
**Files:** `src/com/foodapp/auth/`, `src/com/foodapp/ui/LoginPage.java`, `src/com/foodapp/ui/RegisterPage.java`

**What's done:**
- [x] `Customer` POJO (name, email, phone, passwordHash)
- [x] `AuthService` — BCrypt password hashing + MySQL authentication with dual Email and Mobile Number support (`findByEmailOrPhone`), and automatic offline fallback
- [x] `CustomerDAO` — full JDBC implementation for customers table with `findByEmail`, `findByPhone`, `findByEmailOrPhone`
- [x] `AuthValidator` — email regex, phone (10 digits), `isValidEmailOrPhone`, password (min 6 chars), non-empty checks
- [x] `LoginPage` Swing UI — modern MealFlow branded split layout, real cloud kitchen photo showcase (`kitchen_green.jpg`), circular avatar (`avatar.jpg`), active "← Back to Marketplace" navigation, Google/Apple SSO simulation, 1-Click test credentials auto-fill (`rahul@example.com` / `password123`), forgot password notification, navigates to `HomePage` / `ExplorePage` on success
- [x] `RegisterPage` Swing UI — real macro bowl food photo showcase (`dish_quinoa_avocado.jpg`), live password strength meter, 1-Click verified test registration auto-fill, interactive Terms of Service & Privacy dialog, active "← Back to Marketplace" navigation, validation & terms check


---

### ✅ Person 2 — Meal Plan & Explore Module
**Files:** `src/com/foodapp/mealplan/`, `src/com/foodapp/ui/ExplorePage.java`, `src/com/foodapp/ui/HomePage.java`, `src/com/foodapp/ui/MealPlansPage.java`, `src/com/foodapp/ui/MealPlanDetailsPage.java`, `src/com/foodapp/ui/UIStyle.java`

**What's done:**
- [x] `MealPlan` abstract class (planId, name, description, duration, mealsPerDay, price, foodItems)
- [x] `CookedMealPlan` — concrete subclass for ready-to-eat plans
- [x] `MealKitPlan` — concrete subclass for DIY ingredient kits
- [x] `FoodItem` — entity with name, description, mealType, price, vegetarian flag
- [x] `MealPlanDAO` & `FoodItemDAO` — DB queries with automatic fallback to `SampleData` if MySQL is unreachable
- [x] `MealPlanService` & `FoodItemService` — business layer for meal plans & items
- [x] `SampleData` — 4 hardcoded plans (2 Cooked, 2 Meal Kit) for offline demo
- [x] `ExplorePage` Swing UI — high-fidelity MealFlow Explore dashboard mirroring modern Tailwind design (fixed sidebar with active plan widget, live header with address selector, instant search, notifications, dynamic cart counter, delivery window status, interactive filter chips, curated meal-plan hero banner, 7 meal-type category tiles, 6 high-fidelity dish cards with macros and quantity steppers, and featured neighborhood kitchens)
- [x] `HomePage` Swing UI — extends `ExplorePage` for unified seamless landing experience
- [x] `MealPlansPage` Swing UI — grid of plan cards with type filter dropdown
- [x] `MealPlanDetailsPage` Swing UI — plan summary + food items table + "Select & Customize" button
- [x] `ProfilePage` Swing UI — user avatar, account info, subscription status, back & logout actions
- [x] `UIStyle` — shared theme (green primary, fonts, FlatLaf setup, header with profile button, button/field/card stylers)

---

### ✅ Person 3 — Meal Customization Module
**Files:** `src/com/foodapp/ui/MealCustomizationPage.java`

**What's done:**
- [x] `MealCustomizationPage` Swing UI — full two-column layout (item list + order summary)
- [x] Filter bar with chip buttons: All / Veg / Non-Veg with live item list rebuild
- [x] Select All / Clear All convenience buttons
- [x] Per-item `ItemRow` inner class (encapsulation) with `JCheckBox` + `JSpinner` (qty 1–5)
- [x] Item cards: name, veg/non-veg badge, description, meal type, price per serving
- [x] Live order summary (base price + extra qty cost → total)
- [x] Special Instructions free-text area
- [x] Confirmation dialog before proceeding to `PaymentPage`
- [x] Wired into `MealPlanDetailsPage` — "Select & Customize" button launches the page

---

### ✅ Person 4 (Adarsh) — Payment & Checkout Module
**Files:** `src/com/foodapp/payment/`, `src/com/foodapp/ui/PaymentPage.java`, `src/com/foodapp/ui/PaymentResultPage.java`, `src/com/foodapp/ui/MealPricingDetailsPage.java`

**What's done:**
- [x] `Payment` abstract class — amount, status (PENDING/SUCCESS/FAILED), transactionId, message
- [x] `UPIPayment` — validates UPI ID format (`name@bank`)
- [x] `CardPayment` — validates 16-digit card number, MM/YY expiry, 3-digit CVV
- [x] `WalletPayment` — checks balance sufficiency before deducting
- [x] `CODPayment` — validates delivery address is non-empty
- [x] `PaymentDAO` & `PaymentService` — persistent payment records and transaction state
- [x] `MealPricingDetailsPage` Swing UI — high-fidelity 2-column dish customization & nutrition breakdown modal (4:3 food visual with bestseller & veg badges, animated SVG-style Macro Donut Chart for Protein/Carbs/Fats, 4 macro progress bars, base radio selector, protein/sides checkboxes, live unit & total price calculations, quantity stepper, and animated toast notifications)
- [x] `PaymentPage` Swing UI — complete modern on-demand checkout dashboard (navigation sidebar with active plan widget, address selector, search with ⌘K, vendor banner, dishes in cart with live quantity steppers, multiple delivery address tiles, instant rider instruction pills, MealFlow Cash balance redemption toggle, Indian context payment selector with verified UPI, Credit/Debit cards, Cash on Delivery, promo code 'SAVEMF' panel, calorie sparkline gauge, and atomic checkout order persistence)
- [x] `PaymentResultPage` Swing UI — modern receipt card with circular status icons (green checkmark / red cross), itemized payment summary, dispatch timeline notice, and live delivery tracking navigator
- [x] `PaymentTest` — CLI test runner covering valid + invalid inputs for all 4 methods

---

### ✅ Person 5 — Orders, Subscriptions & Database Module
**Files:** `src/com/foodapp/order/`, `src/com/foodapp/subscription/`, `src/com/foodapp/util/DBConnection.java`, `src/com/foodapp/ui/MyOrdersPage.java`, `sql/`

**What's done:**
- [x] `DBConnection.java` — production-grade connection manager with ping diagnostics, configurable via ENV variables or `db.properties`, and automatic fallback
- [x] `Order`, `OrderItem`, `OrderStatus`, `OrderStatusHistory` models
- [x] `OrderDAO`, `OrderItemDAO`, `OrderStatusHistoryDAO` JDBC implementations
- [x] `OrderService` & `DeliveryTrackingService` business layer
- [x] `Subscription`, `SubscriptionCalendarEntry`, `SubscriptionStatus` models + DAOs + service
- [x] `MyOrdersPage` Swing UI — order history cards, items breakdown, status pill badges, live tracking
- [x] `sql/schema.sql` — 10 normalized tables (InnoDB, utf8mb4) with constraints and indexes
- [x] `sql/seed.sql` & `sql/migration_v1_to_v2.sql` — seed demo data and migrations
- [x] `BackendTestSuite.java` — 74 automated unit & integration tests covering all 5 modules

---

## How to Compile & Run

### Prerequisites
- Java 11 or higher installed
- Terminal opened at the project root (`OOPS PROJECT/`)

### Step 1 — Compile (no MySQL needed)
```bash
javac -cp "lib/*:." -d out $(find src -name "*.java")
```
Expected output: *no errors*, and an `out/` folder is created with all `.class` files.

### Step 2 — Run the App
```bash
java -cp "lib/*:out" com.foodapp.ui.LoginPage
```

### Step 3 — Run Backend Test Suite
```bash
javac -cp "lib/*:out" -d out $(find test -name "*.java")
java -cp "lib/*:out" com.foodapp.test.BackendTestSuite
```

### Step 4 — Run Payment Tests (CLI)
```bash
java -cp "lib/*:out" com.foodapp.payment.PaymentTest
```

---

## What You Will See (UI Flow)

```
[LoginPage]
  ├─ Sign in with email + password (BCrypt verified)
  │    ├─ Validation errors shown in pop-up / inline banner
  │    └─ On success → [ExplorePage / HomePage]
  │
  └─ Click Register → [RegisterPage]
       ├─ Fill name, email, phone, password (live strength meter)
       ├─ Validation & Terms check
       └─ On success → back to [LoginPage]

[ExplorePage / HomePage] (MealFlow Dashboard)
  ├─ Top Bar: Address Selector, Search, Notifications, Cart badge, Profile
  ├─ Left Sidebar: Navigation tabs (Explore, Meal Plans, My Orders, Subscriptions, Settings)
  ├─ Hero Banner: Active/Curated meal plan subscription banner
  ├─ Category Chips: All, Chef's Special, High Protein, Low Carb, Keto, Vegan, Comfort
  ├─ Dish Cards: Image, macros, veg indicator, price, quantity stepper + Add to Cart
  ├─ Click Dish Card Banner / Title → [MealPricingDetailsPage]
  │    ├─ 4:3 Food visual with bestseller & veg badges
  │    ├─ Macro Donut chart (Protein 28%, Carbs 48%, Fats 24%) & 4 macro meters
  │    ├─ Bases (Brown Basmati, Quinoa, Cauliflower) + Protein & Sides add-ons
  │    ├─ Quantity stepper + live dynamic price calculation
  │    ├─ "Add to Weekly Plan" / "Add to Cart" with toast notification
  │    └─ "Proceed to Payment" → [PaymentPage]
  ├─ Featured Neighborhood Kitchens
  ├─ "Browse Meal Plans" → [MealPlansPage]
  ├─ "My Orders" tab/button → [MyOrdersPage]
  └─ Profile button → [ProfilePage]

[MealPlansPage]
  ├─ Filter dropdown: All Plans / Cooked Meals / Meal Kit
  ├─ Grid of plan cards (name, description, type, duration, price)
  └─ "View Details" on any card → [MealPlanDetailsPage]

[MealPlanDetailsPage]
  ├─ Plan summary (type, duration, meals/day, price, prepareMeal())
  ├─ Food items table (meal type, name, description, veg/non-veg, price)
  ├─ "< Back to Plans" → [MealPlansPage]
  └─ "Select & Customize" → [MealCustomizationPage]

[MealCustomizationPage]
  ├─ Filter chips: All / Veg / Non-Veg
  ├─ Select All / Clear All buttons
  ├─ Item list with check boxes & serving quantity spinners (1–5)
  ├─ Special Instructions text box
  ├─ Order Summary card (base price, extras, total)
  └─ "Proceed to Payment" → [PaymentPage]

[PaymentPage] (High-Fidelity On-Demand Checkout)
  ├─ Fixed navigation sidebar + live search header with address selector
  ├─ Consolidated vendor banner ("The Green Kitchen")
  ├─ Dynamic cart dish cards with quantity steppers and line price recalculations
  ├─ Delivery address tiles: Home (Default) vs Work + Add New Address
  ├─ Rider preferences pills ("Avoid calling", "Leave with security", etc.) + delivery notes
  ├─ MealFlow Cash balance widget (₹460 available, "Apply ₹200" checkbox)
  ├─ Payment mode cards: Verified UPI ID with VPA change, Credit/Debit cards, Cash on Delivery
  ├─ Promo code panel ('SAVEMF' ₹50 discount apply/remove)
  ├─ Macro sparkline gauge (840 kcal, 38g Protein, 54g Carbs, 18g Fats)
  ├─ Itemized bill breakdown (subtotal, packaging, delivery partner fee, GST, total savings)
  └─ "Place Order • ₹<total>" → OOP payment validation → atomic order persistence → [PaymentResultPage]

[PaymentResultPage]
  ├─ SUCCESS (green checkmark): Method, Amount, Transaction ID, 30-35 min dispatch notice
  │    ├─ "View My Orders & Live Tracking" → [MyOrdersPage]
  │    └─ "Back to Explore" → [ExplorePage]
  └─ FAILED (red cross): Method, Amount, failure diagnosis
       └─ "Try Again" → back to [PaymentPage]

[MyOrdersPage]
  ├─ Customer order history list
  ├─ Itemized summary & prices
  └─ Order status timeline & live delivery tracking
```

---

## Running with MySQL (Optional)

1. Start MySQL and run the schema:
   ```bash
   mysql -u root -p < sql/schema.sql
   mysql -u root -p < sql/seed.sql
   ```
2. Configure credentials via environment variables:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/mealplan_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   export DB_USER="root"
   export DB_PASSWORD="your_password"
   ```
   *(Or edit `src/com/foodapp/util/DBConnection.java` or `db.properties`)*

> **Note:** If MySQL is unreachable, the app automatically falls back to `SampleData` 
> for meal plans and offline auth, so the UI is always 100% demo-ready without a database.
