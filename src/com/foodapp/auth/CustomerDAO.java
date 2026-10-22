package com.foodapp.auth;

import com.foodapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerDAO - Data Access Object for Customer persistence in MySQL.
 */
public class CustomerDAO {

    public boolean createCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone, password_hash, is_active) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail().toLowerCase().trim());
            ps.setString(3, customer.getPhone().trim());
            ps.setString(4, customer.getPassword()); // hashed password
            ps.setBoolean(5, customer.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        customer.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public Customer findByEmail(String email) throws SQLException {
        if (email == null) return null;
        String sql = "SELECT customer_id, name, email, phone, password_hash, is_active, created_at " +
                     "FROM customers WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Customer findByPhone(String phone) throws SQLException {
        if (phone == null) return null;
        String sql = "SELECT customer_id, name, email, phone, password_hash, is_active, created_at " +
                     "FROM customers WHERE phone = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Customer findByEmailOrPhone(String identifier) throws SQLException {
        if (identifier == null) return null;
        String trimmed = identifier.trim();
        String sql = "SELECT customer_id, name, email, phone, password_hash, is_active, created_at " +
                     "FROM customers WHERE email = ? OR phone = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, trimmed.toLowerCase());
            ps.setString(2, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, name, email, phone, password_hash, is_active, created_at " +
                     "FROM customers WHERE customer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone = ?, is_active = ? WHERE customer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setBoolean(3, customer.isActive());
            ps.setInt(4, customer.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, email, phone, password_hash, is_active, created_at FROM customers ORDER BY customer_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password_hash"),
                rs.getBoolean("is_active")
        );
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }
}
