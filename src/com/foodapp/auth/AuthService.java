package com.foodapp.auth;

import com.foodapp.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthService - Database-backed authentication and customer credential service.
 * <p>
 * Implements salted BCrypt password hashing, unique email validation,
 * active account verification, and session state management.
 * Includes graceful offline fallback for demonstration when MySQL is unreachable.
 * </p>
 */
public class AuthService {

    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static Customer currentUser;

    // In-memory fallback list used exclusively when MySQL is unreachable
    private static final List<Customer> fallbackCustomers = new ArrayList<>();

    static {
        // Initialize default demo users in fallback store
        Customer c1 = new Customer(1, "Rahul Sharma", "rahul@example.com", "9876543210",
                BCrypt.hashpw("password123", BCrypt.gensalt(10)), true);
        Customer c2 = new Customer(2, "Adithya V", "adithya@example.com", "9845012345",
                BCrypt.hashpw("password123", BCrypt.gensalt(10)), true);
        fallbackCustomers.add(c1);
        fallbackCustomers.add(c2);
    }

    /**
     * Registers a new customer with salted BCrypt password hashing.
     *
     * @param c customer entity containing plain text password
     * @return true if registered successfully, false if email already exists or registration fails
     */
    public static boolean register(Customer c) {
        if (c == null || !AuthValidator.isValidEmail(c.getEmail()) || !AuthValidator.isValidPassword(c.getPassword())) {
            return false;
        }

        String plainPassword = c.getPassword();
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        c.setPassword(hashedPassword);

        try {
            // Check if email already registered in MySQL
            Customer existing = customerDAO.findByEmail(c.getEmail());
            if (existing != null) {
                return false;
            }
            return customerDAO.createCustomer(c);

        } catch (SQLException e) {
            if (DBConnection.isFallbackAllowed()) {
                System.err.println("[AuthService] MySQL unavailable (" + e.getMessage() + "). Using in-memory fallback register.");
                for (Customer x : fallbackCustomers) {
                    if (x.getEmail().equalsIgnoreCase(c.getEmail())) {
                        return false;
                    }
                }
                c.setId(fallbackCustomers.size() + 101);
                fallbackCustomers.add(c);
                return true;
            } else {
                System.err.println("[AuthService] Database error during registration: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Authenticates a customer by verifying their email and BCrypt hashed password.
     *
     * @param email customer email
     * @param plainPassword plain text password entered at login
     * @return authenticated Customer object, or null on bad credentials
     */
    public static Customer login(String email, String plainPassword) {
        if (!AuthValidator.isNotEmpty(email) || !AuthValidator.isNotEmpty(plainPassword)) {
            return null;
        }

        try {
            Customer c = customerDAO.findByEmail(email);
            if (c != null) {
                if (!c.isActive()) {
                    System.err.println("[AuthService] Inactive customer account: " + email);
                    return null;
                }
                if (BCrypt.checkpw(plainPassword, c.getPassword())) {
                    currentUser = c;
                    return c;
                }
            }
            return null;

        } catch (SQLException e) {
            if (DBConnection.isFallbackAllowed()) {
                System.err.println("[AuthService] MySQL unavailable (" + e.getMessage() + "). Checking offline fallback users.");
                for (Customer c : fallbackCustomers) {
                    if (c.getEmail().equalsIgnoreCase(email) && c.isActive()) {
                        if (BCrypt.checkpw(plainPassword, c.getPassword()) || plainPassword.equals(c.getPassword())) {
                            currentUser = c;
                            return c;
                        }
                    }
                }
            } else {
                System.err.println("[AuthService] Database error during login: " + e.getMessage());
            }
            return null;
        }
    }

    public static Customer getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Customer user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static CustomerDAO getCustomerDAO() {
        return customerDAO;
    }
}
