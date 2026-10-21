package com.foodapp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection - Centralized JDBC connection and configuration manager.
 * <p>
 * Configuration resolution priority:
 * 1. Environment variables: DB_URL, DB_USER, DB_PASSWORD (or DB_PASS)
 * 2. Local config file: db.properties (in project root or working directory)
 * 3. Safe development defaults
 * </p>
 * Supports explicit fallback policy toggling for testing and production modes.
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/mealplan_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    /**
     * Controls whether DAOs are permitted to fall back to in-memory SampleData
     * when MySQL is unreachable. Enabled by default for college demo convenience,
     * but can be disabled during automated backend integration tests.
     */
    private static volatile boolean fallbackAllowed = true;

    static {
        loadConfiguration();
    }

    private DBConnection() {
        // Prevent instantiation of utility class
    }

    /**
     * Loads database credentials from environment variables or local property files.
     */
    public static synchronized void loadConfiguration() {
        Properties props = new Properties();

        // 1. Try loading from optional db.properties file
        File configFile = new File("db.properties");
        if (configFile.exists() && configFile.canRead()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("[DBConnection] Note: Could not read db.properties (" + e.getMessage() + "), using env/defaults.");
            }
        }

        // 2. Read from Environment or Properties with fallback
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass == null) {
            envPass = System.getenv("DB_PASS");
        }

        dbUrl  = (envUrl != null && !envUrl.trim().isEmpty())
                ? envUrl.trim()
                : props.getProperty("db.url", DEFAULT_URL).trim();

        dbUser = (envUser != null && !envUser.trim().isEmpty())
                ? envUser.trim()
                : props.getProperty("db.user", DEFAULT_USER).trim();

        dbPass = (envPass != null)
                ? envPass
                : props.getProperty("db.password", DEFAULT_PASS);

        // Ensure JDBC driver class is registered
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver may also be auto-loaded via SPI if on classpath
        }
    }

    /**
     * Provides an active JDBC Connection.
     * Callers must close the returned connection (preferably via try-with-resources).
     *
     * @return a fresh {@link Connection} to the MySQL database
     * @throws SQLException if a database access error occurs or credentials are invalid
     */
    public static Connection getConnection() throws SQLException {
        if (dbUrl == null || dbUser == null) {
            loadConfiguration();
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    /**
     * Performs a lightweight connection probe to check if MySQL is online and responsive.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean isAvailable() {
        try (Connection con = getConnection()) {
            return con != null && !con.isClosed() && con.isValid(2);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Returns whether DAOs should fall back to SampleData on connection error.
     */
    public static boolean isFallbackAllowed() {
        return fallbackAllowed;
    }

    /**
     * Enables or disables offline sample-data fallback.
     * Useful to set to false during integration tests to guarantee that SQL queries are exercised.
     *
     * @param allowed true to allow fallback to SampleData, false to fail fast with SQLException
     */
    public static void setFallbackAllowed(boolean allowed) {
        fallbackAllowed = allowed;
    }

    /**
     * Diagnostic helper returning the current active database URL (without leaking password).
     */
    public static String getActiveUrl() {
        return dbUrl;
    }

    /**
     * Diagnostic helper returning the current active database user.
     */
    public static String getActiveUser() {
        return dbUser;
    }
}
