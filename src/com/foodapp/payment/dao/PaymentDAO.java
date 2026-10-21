package com.foodapp.payment.dao;

import com.foodapp.payment.PaymentRecord;
import com.foodapp.payment.PaymentStatus;
import com.foodapp.util.DBConnection;

import java.sql.*;

/**
 * PaymentDAO - Data Access Object for persisting payment records in MySQL.
 */
public class PaymentDAO {

    public boolean createPaymentRecord(PaymentRecord record, Connection con) throws SQLException {
        String sql = "INSERT INTO payments (order_id, payment_method, amount, payment_status, transaction_ref, payment_details) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getOrderId());
            ps.setString(2, record.getPaymentMethod());
            ps.setDouble(3, record.getAmount());
            ps.setString(4, record.getPaymentStatus().name());
            ps.setString(5, record.getTransactionRef());
            ps.setString(6, record.getPaymentDetails());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        record.setPaymentId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public PaymentRecord getPaymentByOrderId(int orderId) throws SQLException {
        String sql = "SELECT payment_id, order_id, payment_method, amount, payment_status, transaction_ref, payment_details, created_at, updated_at " +
                     "FROM payments WHERE order_id = ? ORDER BY payment_id DESC LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public PaymentRecord getPaymentById(int paymentId) throws SQLException {
        String sql = "SELECT payment_id, order_id, payment_method, amount, payment_status, transaction_ref, payment_details, created_at, updated_at " +
                     "FROM payments WHERE payment_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updatePaymentStatus(int paymentId, PaymentStatus status, String txnRef, Connection con) throws SQLException {
        String sql = "UPDATE payments SET payment_status = ?, transaction_ref = ? WHERE payment_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, txnRef);
            ps.setInt(3, paymentId);
            return ps.executeUpdate() > 0;
        }
    }

    private PaymentRecord mapRow(ResultSet rs) throws SQLException {
        PaymentRecord p = new PaymentRecord();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setOrderId(rs.getInt("order_id"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        p.setTransactionRef(rs.getString("transaction_ref"));
        p.setPaymentDetails(rs.getString("payment_details"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
