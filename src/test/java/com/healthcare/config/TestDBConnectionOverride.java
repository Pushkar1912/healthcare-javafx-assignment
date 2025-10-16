package com.healthcare.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Override DBConnection for testing to use H2 test database
 */
public class TestDBConnectionOverride {
    
    private static Connection testConnection;
    
    /**
     * Get test database connection instead of production
     */
    public static Connection getConnection() throws SQLException {
        if (testConnection == null || testConnection.isClosed()) {
            testConnection = TestDBConnection.getConnection();
        }
        return testConnection;
    }
    
    /**
     * Close test connection
     */
    public static void closeConnection() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }
}
