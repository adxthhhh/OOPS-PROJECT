# Food Delivery App — OOPS Project

A Java Swing desktop application for a meal subscription & food delivery service.

## Team Assignments

| Person | Module | Package |
|--------|--------|---------|
| Person 1 | Auth (Login / Register) | `com.foodapp.auth`, `com.foodapp.ui` |
| Person 2 | Meal Plans (Browse / Details) | `com.foodapp.mealplan`, `com.foodapp.ui` |
| Person 3 | Meal Customization (TODO) | TBD |
| Person 4 (Adarsh) | Payment Processing | `com.foodapp.payment`, `com.foodapp.ui` |
| Person 5 | Orders + DB Connection | `com.foodapp.util.DBConnection` |

## Project Structure

```
src/com/foodapp/
├── auth/          Customer, AuthService, AuthValidator
├── ui/            All Swing pages + UIStyle
├── mealplan/
│   ├── model/     MealPlan, CookedMealPlan, MealKitPlan, FoodItem
│   └── dao/       MealPlanDAO, FoodItemDAO, SampleData
├── payment/       Payment (abstract), UPI/Card/Wallet/COD implementations
└── util/          DBConnection (Person 5 replaces this)

test/com/foodapp/
└── payment/       PaymentTest

sql/
└── schema.sql     MySQL database schema + seed data
```

## How to Compile & Run

### Without MySQL (sample data fallback)
```bash
javac -d out $(find src -name "*.java")
java -cp out com.foodapp.ui.LoginPage
```

### With MySQL
1. Run `sql/schema.sql` in MySQL
2. Update credentials in `src/com/foodapp/util/DBConnection.java`
3. Add `mysql-connector-j` JAR to classpath:
```bash
javac -cp mysql-connector-j.jar -d out $(find src -name "*.java")
java -cp out:mysql-connector-j.jar com.foodapp.ui.LoginPage
```

## Integration Points

- **Person 1 → Person 2**: After login, `LoginPage` calls `new HomePage(customer.getName()).setVisible(true)`
- **Person 2 → Person 3**: Replace the TODO in `MealPlanDetailsPage` ("Select & Customize")
- **Person 2/4 → Person 5**: Replace `DBConnection.java` and add the My Orders page hook in `HomePage`
- **Person 4 → Person 2**: `PaymentPage` is launched from `MealPlanDetailsPage` with the plan price
