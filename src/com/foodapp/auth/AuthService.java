package com.foodapp.auth;

import java.util.ArrayList;
import java.util.List;

/** Temporary in-memory store. Person 5 will replace with JDBC calls. */
public class AuthService {
    private static final List<Customer> customers = new ArrayList<>();
    private static Customer currentUser;

    public static boolean register(Customer c) {
        for (Customer x : customers)
            if (x.getEmail().equalsIgnoreCase(c.getEmail())) return false;
        c.setId(customers.size() + 1);
        customers.add(c);
        return true;
    }

    public static Customer login(String email, String password) {
        for (Customer c : customers)
            if (c.getEmail().equalsIgnoreCase(email) && c.getPassword().equals(password)) {
                currentUser = c;
                return c;
            }
        return null;
    }

    public static Customer getCurrentUser() { return currentUser; }
    public static void logout() { currentUser = null; }
}
