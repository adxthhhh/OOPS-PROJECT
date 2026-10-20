package com.foodapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * PLACEHOLDER - Person 5 owns this class.
 * Replace this file with theirs once it is ready (keep getConnection()).
 */
public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/mealplan_db";
    private static final String USER = "root";
    private static final String PASS = "your_password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
