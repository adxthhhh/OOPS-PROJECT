package com.foodapp.subscription.dao;

import com.foodapp.subscription.model.Subscription;
import com.foodapp.subscription.model.SubscriptionStatus;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SubscriptionDAO - Data Access Object for customer meal subscriptions in MySQL.
 */
public class SubscriptionDAO {

    public boolean createSubscription(Subscription sub) throws SQLException {
        String sql = "INSERT INTO subscriptions (customer_id, plan_id, agreed_price, start_date, end_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, sub.getCustomerId());
            ps.setInt(2, sub.getPlanId());
            ps.setDouble(3, sub.getAgreedPrice());
            ps.setDate(4, Date.valueOf(sub.getStartDate()));
            ps.setDate(5, Date.valueOf(sub.getEndDate()));
            ps.setString(6, sub.getStatus().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        sub.setSubscriptionId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public Subscription getSubscriptionById(int subId) throws SQLException {
        String sql = "SELECT s.subscription_id, s.customer_id, s.plan_id, s.agreed_price, s.start_date, s.end_date, " +
                     "s.status, s.created_at, s.updated_at, p.name as plan_name " +
                     "FROM subscriptions s " +
                     "JOIN meal_plans p ON s.plan_id = p.plan_id " +
                     "WHERE s.subscription_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Subscription> getSubscriptionsByCustomer(int customerId) throws SQLException {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT s.subscription_id, s.customer_id, s.plan_id, s.agreed_price, s.start_date, s.end_date, " +
                     "s.status, s.created_at, s.updated_at, p.name as plan_name " +
                     "FROM subscriptions s " +
                     "JOIN meal_plans p ON s.plan_id = p.plan_id " +
                     "WHERE s.customer_id = ? ORDER BY s.subscription_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int subId, SubscriptionStatus status) throws SQLException {
        String sql = "UPDATE subscriptions SET status = ? WHERE subscription_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, subId);
            return ps.executeUpdate() > 0;
        }
    }

    private Subscription mapRow(ResultSet rs) throws SQLException {
        Subscription s = new Subscription();
        s.setSubscriptionId(rs.getInt("subscription_id"));
        s.setCustomerId(rs.getInt("customer_id"));
        s.setPlanId(rs.getInt("plan_id"));
        s.setPlanName(rs.getString("plan_name"));
        s.setAgreedPrice(rs.getDouble("agreed_price"));
        s.setStartDate(rs.getDate("start_date").toLocalDate());
        s.setEndDate(rs.getDate("end_date").toLocalDate());
        s.setStatus(SubscriptionStatus.valueOf(rs.getString("status")));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    }
}
