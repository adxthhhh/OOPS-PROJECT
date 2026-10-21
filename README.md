# MealFlow — Food Delivery & Meal Subscription App (OOPS Project)

A modern Java Swing desktop application developed for the APJKTU 2024 Scheme, B.Tech CSE Semester 3 OOP Course.

## Team Assignments & Module Status

| Person | Module | Status | Package |
|--------|--------|--------|---------|
| Person 1 | Auth (Login & Register UI) | ✅ Complete | `com.foodapp.auth`, `com.foodapp.ui` |
| Person 2 | Meal Plans & Explore Dashboard | ✅ Complete | `com.foodapp.mealplan`, `com.foodapp.ui` |
| Person 3 | Meal Customization | ⏳ Hook in `MealPlanDetailsPage` | `com.foodapp.ui` |
| Person 4 (Adarsh) | Payment Processing | ✅ Complete | `com.foodapp.payment`, `com.foodapp.ui` |
| Person 5 | Orders + MySQL Backend + Subscriptions | ✅ Complete | `com.foodapp.order`, `com.foodapp.subscription`, `com.foodapp.util` |

---

## Project Architecture

```
UI (Swing + FlatLaf) → Service Layer → DAO Layer → JDBC (MySQL Connector/J) → MySQL (mealplan_db)
```

```
src/com/foodapp/
├── auth/                       Customer, CustomerDAO, AuthService (BCrypt), AuthValidator
├── ui/                         ExplorePage, HomePage, LoginPage, RegisterPage, MyOrdersPage,
│                               MealPlansPage, MealPlanDetailsPage, PaymentPage, PaymentResultPage, UIStyle
├── mealplan/
│   ├── model/                  MealPlan, CookedMealPlan, MealKitPlan, FoodItem
│   ├── dao/                    MealPlanDAO, FoodItemDAO, SampleData
│   └── service/                MealPlanService, FoodItemService
├── order/
│   ├── model/                  Order, OrderItem, OrderStatus, OrderStatusHistory
│   ├── dao/                    OrderDAO, OrderItemDAO, OrderStatusHistoryDAO
│   └── service/                OrderService, DeliveryTrackingService
├── payment/
│   ├── model & abstraction/    Payment (abstract), UPIPayment, CardPayment, WalletPayment, CODPayment
│   ├── dao/                    PaymentDAO, PaymentRecord, PaymentStatus
│   └── service/                PaymentService
├── subscription/
│   ├── model/                  Subscription, SubscriptionStatus, SubscriptionCalendarEntry
│   ├── dao/                    SubscriptionDAO, SubscriptionCalendarDAO
│   └── service/                SubscriptionService
└── util/                       DBConnection (Configurable via ENV / db.properties, Fallback policy)

test/com/foodapp/
├── payment/                    PaymentTest
└── test/                       BackendTestSuite (Comprehensive automated suite - 74 passing tests)

sql/
├── schema.sql                  Full 10-table normalized relational schema (InnoDB, utf8mb4)
├── seed.sql                    Realistic demonstration seed data with BCrypt customer hashes
├── migration_v1_to_v2.sql      Safe schema migration script with backup verification
└── README.md                   Complete database setup and administration guide
```

---

## How to Compile & Run

### 1. Compile All Sources
```bash
javac -cp "lib/*:." -d out $(find src -name "*.java")
```

### 2. Run the Application
```bash
# Launch from Login Screen
java -cp "lib/*:out" com.foodapp.ui.LoginPage

# Or directly open the Explore Dashboard
java -cp "lib/*:out" com.foodapp.ui.ExplorePage
```

### 3. Run Automated Tests
```bash
javac -cp "lib/*:out" -d out $(find test -name "*.java")
java -cp "lib/*:out" com.foodapp.test.BackendTestSuite
```

---

## Database Setup (MySQL)

1. Create and populate the database:
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
   *(Or create a `db.properties` file in the project root)*

*Note: If MySQL is not running, the application gracefully activates its offline demonstration fallback so the UI and viva demonstrations never crash.*
