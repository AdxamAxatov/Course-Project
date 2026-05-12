package com.adxam.warehouse.dal.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Shared configuration for integration tests that hit a real PostgreSQL.
 *
 * Defaults match the developer's local install. Override with env vars in CI:
 *   WAREHOUSE_TEST_URL       jdbc:postgresql://host:port/dbname
 *   WAREHOUSE_TEST_USER
 *   WAREHOUSE_TEST_PASSWORD
 *
 * Tests use a separate database (warehouse_test) so they never touch
 * production data. The database is auto-created on first use.
 */
public final class TestDatabaseConfig {

    public static final String URL = env("WAREHOUSE_TEST_URL",
            "jdbc:postgresql://localhost:5432/warehouse_test");
    public static final String USER = env("WAREHOUSE_TEST_USER", "postgres");
    public static final String PASSWORD = env("WAREHOUSE_TEST_PASSWORD", "Adxamtop2006");

    private static volatile boolean ensured = false;

    private TestDatabaseConfig() {}

    public static synchronized void ensureDatabaseExists() throws SQLException {
        if (ensured) return;
        String adminUrl = URL.replaceFirst("/[^/]+$", "/postgres");
        String dbName = URL.substring(URL.lastIndexOf('/') + 1);
        try (Connection c = DriverManager.getConnection(adminUrl, USER, PASSWORD);
             Statement s = c.createStatement()) {
            try {
                s.execute("CREATE DATABASE " + dbName);
            } catch (SQLException e) {
                if (!"42P04".equals(e.getSQLState())) throw e;
            }
        }
        ensured = true;
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
