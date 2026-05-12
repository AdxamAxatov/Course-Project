package com.adxam.warehouse.dal.db;

import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class SchemaInitializer {
    private static final Logger LOG = Logger.getLogger(SchemaInitializer.class.getName());

    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(64) NOT NULL UNIQUE,
                password_hash VARCHAR(128) NOT NULL,
                salt VARCHAR(64) NOT NULL,
                role VARCHAR(16) NOT NULL
            )""";

    private static final String CREATE_LAPTOPS = """
            CREATE TABLE IF NOT EXISTS laptops (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(128) NOT NULL,
                weight DOUBLE NOT NULL,
                price BIGINT NOT NULL,
                quantity INT NOT NULL,
                os VARCHAR(32),
                cpu VARCHAR(64),
                battery_capacity INT
            )""";

    private static final String CREATE_OVENS = """
            CREATE TABLE IF NOT EXISTS ovens (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(128) NOT NULL,
                weight DOUBLE NOT NULL,
                price BIGINT NOT NULL,
                quantity INT NOT NULL,
                power_consumption INT,
                capacity DOUBLE
            )""";

    private final ConnectionPool pool;
    private final String defaultAdminUser;
    private final String defaultAdminPass;

    public SchemaInitializer(ConnectionPool pool, String defaultAdminUser, String defaultAdminPass) {
        this.pool = pool;
        this.defaultAdminUser = defaultAdminUser;
        this.defaultAdminPass = defaultAdminPass;
    }

    public void run() throws SQLException {
        createSchema();
        seedAdmin();
        LOG.info("Schema initialized");
    }

    public void createSchema() throws SQLException {
        try (Connection c = pool.borrow();
             Statement s = c.createStatement()) {
            s.execute(CREATE_USERS);
            s.execute(CREATE_LAPTOPS);
            s.execute(CREATE_OVENS);
        }
    }

    void seedAdmin() throws SQLException {
        try (Connection c = pool.borrow()) {
            try (PreparedStatement check = c.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE role = ?")) {
                check.setString(1, Role.ADMIN.name());
                try (ResultSet rs = check.executeQuery()) {
                    rs.next();
                    if (rs.getLong(1) > 0) return;
                }
            }
            String salt = PasswordHasher.newSalt();
            String hash = PasswordHasher.hash(defaultAdminPass, salt);
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO users (username, password_hash, salt, role) VALUES (?, ?, ?, ?)")) {
                ins.setString(1, defaultAdminUser);
                ins.setString(2, hash);
                ins.setString(3, salt);
                ins.setString(4, Role.ADMIN.name());
                ins.executeUpdate();
            }
            LOG.info(() -> "Default admin seeded: " + defaultAdminUser);
        }
    }
}
