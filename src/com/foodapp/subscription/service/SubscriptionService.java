package com.foodapp.subscription.service;

import com.foodapp.auth.Customer;
import com.foodapp.auth.CustomerDAO;
import com.foodapp.mealplan.dao.FoodItemDAO;
import com.foodapp.mealplan.dao.MealPlanDAO;
import com.foodapp.mealplan.model.FoodItem;
import com.foodapp.mealplan.model.MealPlan;
import com.foodapp.subscription.dao.SubscriptionCalendarDAO;
import com.foodapp.subscription.dao.SubscriptionDAO;
import com.foodapp.subscription.model.Subscription;
import com.foodapp.subscription.model.SubscriptionCalendarEntry;
import com.foodapp.subscription.model.SubscriptionStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * SubscriptionService - Manages customer subscriptions, lifecycle status, and scheduled meals calendar.
 */
public class SubscriptionService {

    private final SubscriptionDAO subscriptionDAO;
    private final SubscriptionCalendarDAO calendarDAO;
    private final MealPlanDAO mealPlanDAO;
    private final FoodItemDAO foodItemDAO;
    private final CustomerDAO customerDAO;

    public SubscriptionService() {
        this.subscriptionDAO = new SubscriptionDAO();
        this.calendarDAO = new SubscriptionCalendarDAO();
        this.mealPlanDAO = new MealPlanDAO();
        this.foodItemDAO = new FoodItemDAO();
        this.customerDAO = new CustomerDAO();
    }

    /**
     * Subscribes a customer to a meal plan starting from the specified date.
     */
    public Subscription subscribeCustomer(int customerId, int planId, LocalDate startDate) throws SQLException {
        if (customerId <= 0) throw new IllegalArgumentException("Invalid customer ID.");
        if (planId <= 0) throw new IllegalArgumentException("Invalid plan ID.");
        if (startDate == null || startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Subscription start date cannot be in the past.");
        }

        Customer customer = customerDAO.findById(customerId);
        if (customer == null || !customer.isActive()) {
            throw new IllegalArgumentException("Customer does not exist or account is inactive.");
        }

        MealPlan plan = mealPlanDAO.getPlanById(planId);
        if (plan == null || !plan.isActive()) {
            throw new IllegalArgumentException("Meal plan not found or inactive.");
        }

        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        Subscription sub = new Subscription(
                customerId,
                planId,
                plan.getPrice(),
                startDate,
                endDate
        );
        sub.setPlanName(plan.getName());

        boolean ok = subscriptionDAO.createSubscription(sub);
        if (!ok || sub.getSubscriptionId() <= 0) {
            throw new SQLException("Failed to create subscription record.");
        }

        return sub;
    }

    public List<Subscription> getCustomerSubscriptions(int customerId) throws SQLException {
        if (customerId <= 0) throw new IllegalArgumentException("Invalid customer ID.");
        return subscriptionDAO.getSubscriptionsByCustomer(customerId);
    }

    public Subscription getSubscriptionDetails(int subscriptionId) throws SQLException {
        if (subscriptionId <= 0) return null;
        return subscriptionDAO.getSubscriptionById(subscriptionId);
    }

    public boolean pauseSubscription(int subscriptionId) throws SQLException {
        Subscription sub = getSubscriptionDetails(subscriptionId);
        if (sub == null) throw new IllegalArgumentException("Subscription not found.");
        if (!sub.getStatus().canPause()) {
            throw new IllegalStateException("Only ACTIVE subscriptions can be paused. Current state: " + sub.getStatus());
        }
        return subscriptionDAO.updateStatus(subscriptionId, SubscriptionStatus.PAUSED);
    }

    public boolean resumeSubscription(int subscriptionId) throws SQLException {
        Subscription sub = getSubscriptionDetails(subscriptionId);
        if (sub == null) throw new IllegalArgumentException("Subscription not found.");
        if (!sub.getStatus().canResume()) {
            throw new IllegalStateException("Only PAUSED subscriptions can be resumed. Current state: " + sub.getStatus());
        }
        return subscriptionDAO.updateStatus(subscriptionId, SubscriptionStatus.ACTIVE);
    }

    public boolean cancelSubscription(int subscriptionId) throws SQLException {
        Subscription sub = getSubscriptionDetails(subscriptionId);
        if (sub == null) throw new IllegalArgumentException("Subscription not found.");
        if (!sub.getStatus().canCancel()) {
            throw new IllegalStateException("Cannot cancel a subscription in state: " + sub.getStatus());
        }
        return subscriptionDAO.updateStatus(subscriptionId, SubscriptionStatus.CANCELLED);
    }

    /**
     * Schedules a specific food item for a meal slot in a customer's subscription.
     */
    public SubscriptionCalendarEntry scheduleMeal(int subscriptionId, LocalDate date, String mealType, int itemId, String notes) throws SQLException {
        if (subscriptionId <= 0) throw new IllegalArgumentException("Invalid subscription ID.");
        if (date == null) throw new IllegalArgumentException("Meal date cannot be null.");
        if (mealType == null || mealType.trim().isEmpty()) throw new IllegalArgumentException("Meal type is required.");
        if (itemId <= 0) throw new IllegalArgumentException("Invalid food item ID.");

        Subscription sub = getSubscriptionDetails(subscriptionId);
        if (sub == null) throw new IllegalArgumentException("Subscription #" + subscriptionId + " not found.");

        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot schedule meals for a subscription that is " + sub.getStatus());
        }

        // Validate date falls strictly within subscription window
        if (date.isBefore(sub.getStartDate()) || date.isAfter(sub.getEndDate())) {
            throw new IllegalArgumentException("Scheduled meal date (" + date + ") must fall between subscription start (" +
                    sub.getStartDate() + ") and end (" + sub.getEndDate() + ").");
        }

        // Validate food item exists in catalogue
        FoodItem item = foodItemDAO.getItemById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Food item #" + itemId + " not found in catalogue.");
        }

        SubscriptionCalendarEntry entry = new SubscriptionCalendarEntry(
                subscriptionId,
                date,
                mealType.trim(),
                itemId,
                notes
        );
        entry.setItemName(item.getName());

        boolean ok = calendarDAO.addCalendarEntry(entry);
        if (!ok) {
            throw new SQLException("Failed to save scheduled meal in calendar.");
        }

        return entry;
    }

    public List<SubscriptionCalendarEntry> getScheduledMealsForDate(int subscriptionId, LocalDate date) throws SQLException {
        return calendarDAO.getEntriesForDate(subscriptionId, date);
    }

    public List<SubscriptionCalendarEntry> getWeeklySchedule(int subscriptionId, LocalDate weekStartDate) throws SQLException {
        return calendarDAO.getEntriesForSubscription(subscriptionId);
    }
}
