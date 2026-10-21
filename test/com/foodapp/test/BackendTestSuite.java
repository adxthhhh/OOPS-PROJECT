package com.foodapp.test;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.AuthValidator;
import com.foodapp.auth.Customer;
import com.foodapp.mealplan.dao.MealPlanDAO;
import com.foodapp.mealplan.model.FoodItem;
import com.foodapp.mealplan.model.MealPlan;
import com.foodapp.mealplan.service.FoodItemService;
import com.foodapp.mealplan.service.MealPlanService;
import com.foodapp.order.model.Order;
import com.foodapp.order.model.OrderStatus;
import com.foodapp.order.model.OrderStatusHistory;
import com.foodapp.order.service.DeliveryTrackingService;
import com.foodapp.order.service.OrderService;
import com.foodapp.payment.*;
import com.foodapp.payment.service.PaymentService;
import com.foodapp.subscription.model.Subscription;
import com.foodapp.subscription.model.SubscriptionCalendarEntry;
import com.foodapp.subscription.model.SubscriptionStatus;
import com.foodapp.subscription.service.SubscriptionService;
import com.foodapp.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.*;

/**
 * BackendTestSuite - Comprehensive test runner verifying all backend layers:
 * 1. Database connection & configuration diagnostics
 * 2. BCrypt hashing & Customer Authentication
 * 3. MealPlan & FoodItem Catalogue operations
 * 4. Order & Transactional Checkout calculations
 * 5. Payment Polymorphism & Persistence
 * 6. Subscription lifecycle & Calendar Scheduling validation
 * 7. Delivery Status Lifecycle transitions & Audit History
 */
public class BackendTestSuite {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("  MealFlow Backend Automated Test Suite (OOP & Database Integration)");
        System.out.println("===============================================================================");

        testDatabaseConnectivityDiagnostics();
        testPasswordHashingAndAuthValidation();
        testCustomerRegistrationAndLogin();
        testMealPlanCatalogueAndPolymorphism();
        testOrderCalculationsAndLifecycleStateRules();
        testPaymentHierarchyAndSimulatedProcessing();
        testSubscriptionPeriodAndCalendarDateValidation();
        testDeliveryStatusTransitionConstraints();

        System.out.println("===============================================================================");
        System.out.println(String.format("  TEST RESULTS: Total: %d | PASSED: %d | FAILED: %d", totalTests, passedTests, failedTests));
        System.out.println("===============================================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        totalTests++;
        if (Objects.equals(expected, actual)) {
            passedTests++;
            System.out.println(" [PASS] " + testName);
        } else {
            failedTests++;
            System.err.println(" [FAIL] " + testName + " => Expected: " + expected + ", Got: " + actual);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println(" [PASS] " + testName);
        } else {
            failedTests++;
            System.err.println(" [FAIL] " + testName + " => Expected TRUE, Got FALSE");
        }
    }

    private static void assertFalse(String testName, boolean condition) {
        assertTrue(testName, !condition);
    }

    // -------------------------------------------------------------------------
    // 1. Database Connection Diagnostics
    // -------------------------------------------------------------------------
    private static void testDatabaseConnectivityDiagnostics() {
        System.out.println("\n--- 1. Testing Database Configuration & Diagnostics ---");

        String activeUrl = DBConnection.getActiveUrl();
        assertTrue("DB URL is configured", activeUrl != null && activeUrl.contains("mealplan_db"));

        String activeUser = DBConnection.getActiveUser();
        assertTrue("DB User is configured", activeUser != null && !activeUser.isEmpty());

        boolean isDbOnline = DBConnection.isAvailable();
        System.out.println(" [INFO] MySQL Live Ping Status: " + (isDbOnline ? "ONLINE" : "OFFLINE (Graceful demo fallback active)"));

        boolean initialFallback = DBConnection.isFallbackAllowed();
        DBConnection.setFallbackAllowed(false);
        assertFalse("Can disable sample fallback policy for testing", DBConnection.isFallbackAllowed());
        DBConnection.setFallbackAllowed(initialFallback);
        assertTrue("Restored sample fallback policy", DBConnection.isFallbackAllowed());
    }

    // -------------------------------------------------------------------------
    // 2. BCrypt & Auth Validation
    // -------------------------------------------------------------------------
    private static void testPasswordHashingAndAuthValidation() {
        System.out.println("\n--- 2. Testing Password Hashing & Input Validation ---");

        assertTrue("Valid email accepted", AuthValidator.isValidEmail("rahul@example.com"));
        assertFalse("Malformed email rejected", AuthValidator.isValidEmail("invalid-email"));
        assertFalse("Empty email rejected", AuthValidator.isValidEmail(""));

        assertTrue("10-digit phone accepted", AuthValidator.isValidPhone("9876543210"));
        assertFalse("Short phone rejected", AuthValidator.isValidPhone("12345"));
        assertFalse("Alpha phone rejected", AuthValidator.isValidPhone("98765abcde"));

        assertTrue("6-char password accepted", AuthValidator.isValidPassword("secret"));
        assertFalse("Short password rejected", AuthValidator.isValidPassword("123"));

        // BCrypt salting
        String rawPassword = "collegeProjectPassword2026";
        String hash1 = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        String hash2 = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));

        assertFalse("BCrypt hashes for same password must differ due to unique salts", hash1.equals(hash2));
        assertTrue("Hash1 matches raw password", BCrypt.checkpw(rawPassword, hash1));
        assertTrue("Hash2 matches raw password", BCrypt.checkpw(rawPassword, hash2));
        assertFalse("Wrong password fails verification", BCrypt.checkpw("wrongPassword", hash1));
    }

    // -------------------------------------------------------------------------
    // 3. Customer Registration and Login
    // -------------------------------------------------------------------------
    private static void testCustomerRegistrationAndLogin() {
        System.out.println("\n--- 3. Testing Customer Registration & Authentication ---");

        // Seed customer login
        Customer loggedIn = AuthService.login("rahul@example.com", "password123");
        assertTrue("Known customer can log in", loggedIn != null);
        if (loggedIn != null) {
            assertEquals("Customer name matches", "Rahul Sharma", loggedIn.getName());
            assertEquals("Session currentUser updated", loggedIn, AuthService.getCurrentUser());
        }

        // Bad password
        Customer badLogin = AuthService.login("rahul@example.com", "wrongpass");
        assertTrue("Bad password rejected", badLogin == null);

        // Unknown email
        Customer unknownLogin = AuthService.login("unknown@example.com", "password123");
        assertTrue("Unknown email rejected", unknownLogin == null);

        // Register new customer
        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@example.com";
        Customer newCust = new Customer("Test User", uniqueEmail, "9988776655", "password123");
        boolean registered = AuthService.register(newCust);
        assertTrue("New unique customer registers successfully", registered);

        // Duplicate registration must fail
        Customer dupCust = new Customer("Dup User", uniqueEmail, "9988776655", "password123");
        boolean dupRegistered = AuthService.register(dupCust);
        assertFalse("Duplicate email registration is rejected", dupRegistered);

        // Logout
        AuthService.logout();
        assertTrue("Session cleared on logout", AuthService.getCurrentUser() == null);
    }

    // -------------------------------------------------------------------------
    // 4. Meal Plan Catalogue & Polymorphism
    // -------------------------------------------------------------------------
    private static void testMealPlanCatalogueAndPolymorphism() {
        System.out.println("\n--- 4. Testing Catalogue, Polymorphism, & Fallback ---");

        MealPlanService planService = new MealPlanService();
        List<MealPlan> plans = planService.getAllActivePlans();

        assertTrue("At least 4 meal plans available in catalogue", plans.size() >= 4);

        MealPlan plan1 = planService.getPlanById(1);
        assertTrue("Plan #1 exists", plan1 != null);
        if (plan1 != null) {
            assertEquals("Plan #1 type is Cooked Meals", "Cooked Meals", plan1.getPlanType());
            assertEquals("Plan #1 prepares ready-to-eat", "Preparing ready-to-eat meal", plan1.prepareMeal());
        }

        MealPlan plan3 = planService.getPlanById(3);
        assertTrue("Plan #3 exists", plan3 != null);
        if (plan3 != null) {
            assertEquals("Plan #3 type is Meal Kit", "Meal Kit", plan3.getPlanType());
            assertEquals("Plan #3 packs ingredients", "Packing fresh pre-portioned ingredients and recipe card", plan3.prepareMeal());
        }

        // Food item checks
        FoodItem item = new FoodItem(99, "Test Salad", "Fresh salad", "Lunch", 150.0, true);
        assertTrue("Item is vegetarian", item.isVegetarian());
        assertTrue("Item is available by default", item.isAvailable());
        assertEquals("Price matches", 150.0, item.getPrice());
    }

    // -------------------------------------------------------------------------
    // 5. Order Calculations & Lifecycle State Rules
    // -------------------------------------------------------------------------
    private static void testOrderCalculationsAndLifecycleStateRules() {
        System.out.println("\n--- 5. Testing Order Calculations & Lifecycle Progression ---");

        Order order = new Order(1, 260.0, 40.0, 300.0, "Indiranagar 100ft Rd", "9876543210");
        assertEquals("Initial status is PLACED", OrderStatus.PLACED, order.getOrderStatus());
        assertEquals("Subtotal matches", 260.0, order.getSubtotal());
        assertEquals("Delivery fee matches", 40.0, order.getDeliveryFee());
        assertEquals("Total matches", 300.0, order.getTotalAmount());

        // State Machine validation
        assertTrue("PLACED can transition to CONFIRMED", OrderStatus.PLACED.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue("PLACED can transition to CANCELLED", OrderStatus.PLACED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse("PLACED cannot jump directly to DELIVERED", OrderStatus.PLACED.canTransitionTo(OrderStatus.DELIVERED));

        assertTrue("CONFIRMED can transition to PREPARING", OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PREPARING));
        assertTrue("PREPARING can transition to OUT_FOR_DELIVERY", OrderStatus.PREPARING.canTransitionTo(OrderStatus.OUT_FOR_DELIVERY));
        assertTrue("OUT_FOR_DELIVERY can transition to DELIVERED", OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse("OUT_FOR_DELIVERY cannot be CANCELLED", OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse("DELIVERED is terminal", OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PLACED));
    }

    // -------------------------------------------------------------------------
    // 6. Payment Polymorphism
    // -------------------------------------------------------------------------
    private static void testPaymentHierarchyAndSimulatedProcessing() {
        System.out.println("\n--- 6. Testing Payment Polymorphism & Validation ---");

        Payment upi = new UPIPayment(460.0, "rahul@okaxis");
        assertTrue("Valid UPI payment succeeds", upi.pay());
        assertEquals("UPI method name matches", "UPI", upi.getMethodName());
        assertEquals("Status is SUCCESS", "SUCCESS", upi.getStatus());
        assertTrue("Transaction ID generated", upi.getTransactionId().startsWith("TXN"));

        Payment badUpi = new UPIPayment(460.0, "invalid-upi");
        assertFalse("Invalid UPI ID rejected", badUpi.pay());
        assertEquals("Status is FAILED", "FAILED", badUpi.getStatus());

        Payment card = new CardPayment(460.0, "1111222233334444", "Rahul Sharma", "12/29", "456");
        assertTrue("Valid Card payment succeeds", card.pay());
        assertEquals("Card method name matches", "Card", card.getMethodName());

        Payment badCard = new CardPayment(460.0, "1234", "Rahul", "12/29", "456");
        assertFalse("Short card number rejected", badCard.pay());

        Payment cod = new CODPayment(460.0, "Indiranagar, Bengaluru");
        assertTrue("COD with valid address succeeds", cod.pay());
        assertEquals("COD method name matches", "Cash on Delivery", cod.getMethodName());

        Payment badCod = new CODPayment(460.0, "");
        assertFalse("COD with empty address rejected", badCod.pay());
    }

    // -------------------------------------------------------------------------
    // 7. Subscriptions & Calendar Date Validation
    // -------------------------------------------------------------------------
    private static void testSubscriptionPeriodAndCalendarDateValidation() {
        System.out.println("\n--- 7. Testing Subscription Dates & Scheduling Rules ---");

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        Subscription sub = new Subscription(1, 1, 1799.0, today, nextWeek);
        assertEquals("Status is ACTIVE", SubscriptionStatus.ACTIVE, sub.getStatus());
        assertTrue("Date inside period is active", sub.isActiveOn(today.plusDays(3)));
        assertFalse("Date before start is inactive", sub.isActiveOn(today.minusDays(1)));
        assertFalse("Date after end is inactive", sub.isActiveOn(today.plusDays(10)));

        assertTrue("ACTIVE can pause", sub.getStatus().canPause());
        sub.setStatus(SubscriptionStatus.PAUSED);
        assertTrue("PAUSED can resume", sub.getStatus().canResume());
        assertTrue("PAUSED can cancel", sub.getStatus().canCancel());

        sub.setStatus(SubscriptionStatus.CANCELLED);
        assertFalse("CANCELLED cannot pause", sub.getStatus().canPause());
        assertFalse("CANCELLED cannot resume", sub.getStatus().canResume());

        // Calendar entry validation
        SubscriptionCalendarEntry entry = new SubscriptionCalendarEntry(1, today.plusDays(2), "Lunch", 11, "Less spicy");
        assertEquals("Entry date matches", today.plusDays(2), entry.getMealDate());
        assertEquals("Meal type matches", "Lunch", entry.getMealType());
        assertEquals("Item ID matches", 11, entry.getItemId());
    }

    // -------------------------------------------------------------------------
    // 8. Delivery Tracking & History
    // -------------------------------------------------------------------------
    private static void testDeliveryStatusTransitionConstraints() {
        System.out.println("\n--- 8. Testing Order Status History Auditing ---");

        OrderStatusHistory hist = new OrderStatusHistory(1001, OrderStatus.PLACED, OrderStatus.CONFIRMED, "Payment verified");
        assertEquals("Order ID matches", 1001, hist.getOrderId());
        assertEquals("Old status matches", OrderStatus.PLACED, hist.getOldStatus());
        assertEquals("New status matches", OrderStatus.CONFIRMED, hist.getNewStatus());
        assertTrue("Notes match", hist.getNotes().contains("Payment verified"));
    }
}
