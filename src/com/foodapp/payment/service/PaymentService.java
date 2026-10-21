package com.foodapp.payment.service;

import com.foodapp.order.dao.OrderDAO;
import com.foodapp.order.dao.OrderStatusHistoryDAO;
import com.foodapp.order.model.Order;
import com.foodapp.order.model.OrderStatus;
import com.foodapp.order.model.OrderStatusHistory;
import com.foodapp.payment.CODPayment;
import com.foodapp.payment.Payment;
import com.foodapp.payment.PaymentRecord;
import com.foodapp.payment.PaymentStatus;
import com.foodapp.payment.dao.PaymentDAO;
import com.foodapp.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * PaymentService - Integrates the OOP Payment polymorphism hierarchy with persistent database records.
 */
public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;
    private final OrderStatusHistoryDAO historyDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.orderDAO = new OrderDAO();
        this.historyDAO = new OrderStatusHistoryDAO();
    }

    /**
     * Executes the simulated payment, updates the payment record, and updates order status atomically.
     *
     * @param orderId target order identifier
     * @param payment concrete Payment instance (UPIPayment, CardPayment, WalletPayment, or CODPayment)
     * @return updated PaymentRecord
     * @throws SQLException if a database error occurs
     */
    public PaymentRecord processPaymentForOrder(int orderId, Payment payment) throws SQLException {
        if (orderId <= 0 || payment == null) {
            throw new IllegalArgumentException("Invalid order ID or payment instance.");
        }

        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order #" + orderId + " not found.");
        }

        // Validate payment amount against order total (tolerate minor rounding up to 0.01)
        if (Math.abs(payment.getAmount() - order.getTotalAmount()) > 0.05) {
            throw new IllegalArgumentException("Payment amount (Rs." + payment.getAmount() +
                    ") does not match order total (Rs." + order.getTotalAmount() + ").");
        }

        // Execute OOP polymorphism: validates format, deductions, or addresses
        boolean isSuccess = payment.pay();

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Retrieve or create payment record
                PaymentRecord payRecord = paymentDAO.getPaymentByOrderId(orderId);
                if (payRecord == null) {
                    payRecord = new PaymentRecord(
                            orderId,
                            payment.getMethodName(),
                            payment.getAmount(),
                            PaymentStatus.PENDING,
                            payment.getTransactionId(),
                            payment.getMessage()
                    );
                    paymentDAO.createPaymentRecord(payRecord, con);
                }

                // 2. Determine statuses
                PaymentStatus finalPaymentStatus;
                OrderStatus nextOrderStatus;

                if (payment instanceof CODPayment) {
                    // Cash on Delivery: Payment remains PENDING until physical handover,
                    // but the Order is CONFIRMED for restaurant preparation.
                    finalPaymentStatus = isSuccess ? PaymentStatus.PENDING : PaymentStatus.FAILED;
                    nextOrderStatus = isSuccess ? OrderStatus.CONFIRMED : OrderStatus.PLACED;
                } else {
                    // Online methods (UPI, Card, Wallet)
                    finalPaymentStatus = isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
                    nextOrderStatus = isSuccess ? OrderStatus.CONFIRMED : OrderStatus.PLACED;
                }

                // Update payment record in DB
                payRecord.setPaymentStatus(finalPaymentStatus);
                payRecord.setTransactionRef(payment.getTransactionId());
                payRecord.setPaymentDetails(payment.getMessage());
                paymentDAO.updatePaymentStatus(payRecord.getPaymentId(), finalPaymentStatus, payment.getTransactionId(), con);

                // 3. If confirmed, update Order and Status History
                if (isSuccess && order.getOrderStatus() == OrderStatus.PLACED) {
                    orderDAO.updateOrderStatus(orderId, nextOrderStatus, con);

                    OrderStatusHistory hist = new OrderStatusHistory(
                            orderId,
                            OrderStatus.PLACED,
                            nextOrderStatus,
                            "Payment processed via " + payment.getMethodName() + ": " + payment.getMessage()
                    );
                    historyDAO.addHistoryEntry(hist, con);
                }

                con.commit();
                return payRecord;

            } catch (Exception e) {
                con.rollback();
                throw new SQLException("Payment transaction rolled back: " + e.getMessage(), e);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public PaymentRecord getPaymentForOrder(int orderId) throws SQLException {
        return paymentDAO.getPaymentByOrderId(orderId);
    }
}
