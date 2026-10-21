package com.foodapp.order.dao;

import com.foodapp.order.model.OrderStatus;
import com.foodapp.order.model.OrderStatusHistory;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderStatusHistoryDAO - Data Access Object for chronological order status history timeline.
 */
public class OrderStatusHistoryDAO {

    public boolean addHistoryEntry(OrderStatusHistory history, Connection con) throws SQLException {
        String sql = "INSERT INTO order_status_history (order_id, old_status, new_status, notes) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, history.getOrderId());
            if (history.getOldStatus() != null) {
                ps.setString(2, history.getOldStatus().name());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, history.getNewStatus().name());
            ps.setString(4, history.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        history.setHistoryId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public List<OrderStatusHistory> getHistoryForOrder(int orderId) throws SQLException {
        List<OrderStatusHistory> list = new ArrayList<>();
        String sql = "SELECT history_id, order_id, old_status, new_status, notes, changed_at " +
                     "FROM order_status_history WHERE order_id = ? ORDER BY changed_at ASC, history_id ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String oldStr = rs.getString("old_status");
                    OrderStatus oldStatus = (oldStr != null) ? OrderStatus.valueOf(oldStr) : null;
                    OrderStatus newStatus = OrderStatus.valueOf(rs.getString("new_status"));

                    OrderStatusHistory entry = new OrderStatusHistory(
                            rs.getInt("history_id"),
                            rs.getInt("order_id"),
                            oldStatus,
                            newStatus,
                            rs.getString("notes"),
                            rs.getTimestamp("changed_at")
                    );
                    list.add(entry);
                }
            }
        }
        return list;
    }
}
