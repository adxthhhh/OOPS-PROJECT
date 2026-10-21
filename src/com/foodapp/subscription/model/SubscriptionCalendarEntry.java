package com.foodapp.subscription.model;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Domain entity representing scheduled meals for a specific date and meal slot in a subscription.
 */
public class SubscriptionCalendarEntry {
    private int calendarEntryId;
    private int subscriptionId;
    private LocalDate mealDate;
    private String mealType; // Breakfast, Lunch, Dinner
    private int itemId;
    private String itemName;
    private String customNotes;
    private Timestamp createdAt;

    public SubscriptionCalendarEntry() {}

    public SubscriptionCalendarEntry(int subscriptionId, LocalDate mealDate, String mealType, int itemId, String customNotes) {
        this.subscriptionId = subscriptionId;
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.itemId = itemId;
        this.customNotes = customNotes;
    }

    public int getCalendarEntryId() { return calendarEntryId; }
    public void setCalendarEntryId(int calendarEntryId) { this.calendarEntryId = calendarEntryId; }

    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }

    public LocalDate getMealDate() { return mealDate; }
    public void setMealDate(LocalDate mealDate) { this.mealDate = mealDate; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCustomNotes() { return customNotes; }
    public void setCustomNotes(String customNotes) { this.customNotes = customNotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return mealDate + " [" + mealType + "] Item #" + itemId + (itemName != null ? " (" + itemName + ")" : "") +
                (customNotes != null && !customNotes.isEmpty() ? " Notes: " + customNotes : "");
    }
}
