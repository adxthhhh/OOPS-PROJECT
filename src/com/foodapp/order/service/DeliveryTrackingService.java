package com.foodapp.order.service;

import com.foodapp.order.dao.OrderDAO;
import com.foodapp.order.dao.OrderStatusHistoryDAO;
import com.foodapp.order.model.Order;
import com.foodapp.order.model.OrderStatus;
import com.foodapp.order.model.OrderStatusHistory;
import com.foodapp.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DeliveryTrackingService - Manages delivery status progression, state transitions,
 * and chronological timeline auditing.
 */
public class DeliveryTrackingService {

    private final OrderDAO orderDAO;
    private final OrderStatusHistoryDAO historyDAO;

    public DeliveryTrackingService() {
        this.orderDAO = new OrderDAO();
        this.historyDAO = new OrderStatusHistoryDAO();
    }

    /**
     * Advances an order's status to the next stage in the lifecycle inside an atomic transaction.
     * Validates transition rules to prevent illegal jumps (e.g. DELIVERED -> PLACED).
     */
    public boolean advanceStatus(int orderId, OrderStatus nextStatus, String notes) throws SQLException {
        if (orderId <= 0 || nextStatus == null) {
            throw new IllegalArgumentException("Invalid orderId or nextStatus.");
        }

        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order #" + orderId + " not found.");
        }

        OrderStatus currentStatus = order.getOrderStatus();
        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new IllegalStateException("Illegal status transition from " + currentStatus + " to " + nextStatus);
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Update Order Status
                boolean updated = orderDAO.updateOrderStatus(orderId, nextStatus, con);
                if (!updated) {
                    throw new SQLException("Failed to update status on order #" + orderId);
                }

                // Insert Status History Entry
                OrderStatusHistory hist = new OrderStatusHistory(
                        orderId,
                        currentStatus,
                        nextStatus,
                        (notes != null && !notes.trim().isEmpty()) ? notes.trim() : "Status updated to " + nextStatus
                );
                boolean histAdded = historyDAO.addHistoryEntry(hist, con);
                if (!histAdded) {
                    throw new SQLException("Failed to insert status history entry for order #" + orderId);
                }

                con.commit();
                return true;

            } catch (Exception e) {
                con.rollback();
                throw new SQLException("Status transition aborted and rolled back: " + e.getMessage(), e);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves the complete chronological audit timeline for an order.
     */
    public List<OrderStatusHistory> getOrderTimeline(int orderId) throws SQLException {
        if (orderId <= 0) throw new IllegalArgumentException("Invalid order ID.");
        return historyDAO.getHistoryForOrder(orderId);
    }

    /**
     * Retrieves an order with authorization check ensuring the customer owns the order.
     */
    public Order getAuthorizedOrder(int orderId, int customerId) throws SQLException {
        Order order = orderDAO.getOrderById(orderId);
        if (order == null) return null;
        if (order.getCustomerId() != customerId) {
            throw new SecurityException("Unauthorized access: Customer #" + customerId + " cannot view Order #" + orderId);
        }
        return order;
    }
}
