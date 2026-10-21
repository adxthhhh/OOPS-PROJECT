package com.foodapp.order.dao;

import com.foodapp.order.model.Order;
import com.foodapp.order.model.OrderStatus;
import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDAO - Data Access Object for Order lifecycle persistence.
 */
public class OrderDAO {

    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    /**
     * Inserts an order record as part of a transaction managed by caller's Connection.
     */
    public boolean createOrder(Order order, Connection con) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, order_status, subtotal, delivery_fee, total_amount, " +
                     "delivery_address, contact_phone, estimated_delivery_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            ps.setString(2, order.getOrderStatus().name());
            ps.setDouble(3, order.getSubtotal());
            ps.setDouble(4, order.getDeliveryFee());
            ps.setDouble(5, order.getTotalAmount());
            ps.setString(6, order.getDeliveryAddress());
            ps.setString(7, order.getContactPhone());
            ps.setString(8, order.getEstimatedDeliveryTime());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        order.setOrderId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT order_id, customer_id, order_status, subtotal, delivery_fee, total_amount, " +
                     "delivery_address, contact_phone, estimated_delivery_time, created_at, updated_at " +
                     "FROM orders WHERE order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapRow(rs);
                    order.setItems(orderItemDAO.getItemsForOrder(orderId));
                    return order;
                }
            }
        }
        return null;
    }

    public List<Order> getOrdersByCustomer(int customerId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT order_id, customer_id, order_status, subtotal, delivery_fee, total_amount, " +
                     "delivery_address, contact_phone, estimated_delivery_time, created_at, updated_at " +
                     "FROM orders WHERE customer_id = ? ORDER BY order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapRow(rs);
                    order.setItems(orderItemDAO.getItemsForOrder(order.getOrderId()));
                    list.add(order);
                }
            }
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, OrderStatus status, Connection con) throws SQLException {
        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateOrderStatus(int orderId, OrderStatus status) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return updateOrderStatus(orderId, status, con);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status")));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setDeliveryFee(rs.getDouble("delivery_fee"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setContactPhone(rs.getString("contact_phone"));
        o.setEstimatedDeliveryTime(rs.getString("estimated_delivery_time"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        o.setUpdatedAt(rs.getTimestamp("updated_at"));
        return o;
    }
}
