package com.foodapp.subscription.dao;

import com.foodapp.subscription.model.SubscriptionCalendarEntry;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SubscriptionCalendarDAO - Data Access Object for scheduled day-by-day subscription meals.
 */
public class SubscriptionCalendarDAO {

    public boolean addCalendarEntry(SubscriptionCalendarEntry entry) throws SQLException {
        String sql = "INSERT INTO subscription_calendar (subscription_id, meal_date, meal_type, item_id, custom_notes) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE item_id = VALUES(item_id), custom_notes = VALUES(custom_notes)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, entry.getSubscriptionId());
            ps.setDate(2, Date.valueOf(entry.getMealDate()));
            ps.setString(3, entry.getMealType());
            ps.setInt(4, entry.getItemId());
            ps.setString(5, entry.getCustomNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        entry.setCalendarEntryId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public List<SubscriptionCalendarEntry> getEntriesForSubscription(int subscriptionId) throws SQLException {
        List<SubscriptionCalendarEntry> list = new ArrayList<>();
        String sql = "SELECT sc.calendar_entry_id, sc.subscription_id, sc.meal_date, sc.meal_type, " +
                     "sc.item_id, sc.custom_notes, sc.created_at, f.name as item_name " +
                     "FROM subscription_calendar sc " +
                     "JOIN food_items f ON sc.item_id = f.item_id " +
                     "WHERE sc.subscription_id = ? " +
                     "ORDER BY sc.meal_date ASC, sc.meal_type ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<SubscriptionCalendarEntry> getEntriesForDate(int subscriptionId, LocalDate date) throws SQLException {
        List<SubscriptionCalendarEntry> list = new ArrayList<>();
        String sql = "SELECT sc.calendar_entry_id, sc.subscription_id, sc.meal_date, sc.meal_type, " +
                     "sc.item_id, sc.custom_notes, sc.created_at, f.name as item_name " +
                     "FROM subscription_calendar sc " +
                     "JOIN food_items f ON sc.item_id = f.item_id " +
                     "WHERE sc.subscription_id = ? AND sc.meal_date = ? " +
                     "ORDER BY sc.meal_type ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subscriptionId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean deleteCalendarEntry(int calendarEntryId) throws SQLException {
        String sql = "DELETE FROM subscription_calendar WHERE calendar_entry_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, calendarEntryId);
            return ps.executeUpdate() > 0;
        }
    }

    private SubscriptionCalendarEntry mapRow(ResultSet rs) throws SQLException {
        SubscriptionCalendarEntry e = new SubscriptionCalendarEntry();
        e.setCalendarEntryId(rs.getInt("calendar_entry_id"));
        e.setSubscriptionId(rs.getInt("subscription_id"));
        e.setMealDate(rs.getDate("meal_date").toLocalDate());
        e.setMealType(rs.getString("meal_type"));
        e.setItemId(rs.getInt("item_id"));
        e.setItemName(rs.getString("item_name"));
        e.setCustomNotes(rs.getString("custom_notes"));
        e.setCreatedAt(rs.getTimestamp("created_at"));
        return e;
    }
}
