package com.foodapp.order.dao;

import com.foodapp.order.model.OrderItem;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderItemDAO - Data Access Object for line items within an order.
 */
public class OrderItemDAO {

    public boolean insertOrderItems(List<OrderItem> items, Connection con) throws SQLException {
        if (items == null || items.isEmpty()) return true;

        String sql = "INSERT INTO order_items (order_id, item_id, item_name_snapshot, quantity, unit_price_snapshot, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : items) {
                ps.setInt(1, item.getOrderId());
                ps.setInt(2, item.getItemId());
                ps.setString(3, item.getItemNameSnapshot());
                ps.setInt(4, item.getQuantity());
                ps.setDouble(5, item.getUnitPriceSnapshot());
                ps.setDouble(6, item.getSubtotal());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            return results.length == items.size();
        }
    }

    public List<OrderItem> getItemsForOrder(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT order_item_id, order_id, item_id, item_name_snapshot, quantity, unit_price_snapshot, subtotal " +
                     "FROM order_items WHERE order_id = ? ORDER BY order_item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getInt("order_item_id"),
                            rs.getInt("order_id"),
                            rs.getInt("item_id"),
                            rs.getString("item_name_snapshot"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price_snapshot"),
                            rs.getDouble("subtotal")
                    );
                    list.add(item);
                }
            }
        }
        return list;
    }
}
