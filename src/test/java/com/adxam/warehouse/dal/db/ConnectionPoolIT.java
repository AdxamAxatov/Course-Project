package com.adxam.warehouse.dal.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionPoolIT {

    @BeforeAll
    static void setUp() throws SQLException {
        TestDatabaseConfig.ensureDatabaseExists();
    }

    @Test
    void borrowedConnectionIsReusedAfterClose() throws SQLException {
        try (ConnectionPool pool = new ConnectionPool(
                TestDatabaseConfig.URL, TestDatabaseConfig.USER, TestDatabaseConfig.PASSWORD, 1)) {
            Connection first = pool.borrow();
            assertTrue(first.isValid(1));
            first.close();
            Connection second = pool.borrow();
            assertSame(first, second);
            second.close();
        }
    }

    @Test
    void poolEnforcesCapacityWithTimeout() throws SQLException {
        try (ConnectionPool pool = new ConnectionPool(
                TestDatabaseConfig.URL, TestDatabaseConfig.USER, TestDatabaseConfig.PASSWORD, 1)) {
            Connection only = pool.borrow();
            assertNotNull(only);
            Thread bg = new Thread(() -> {
                try {
                    Connection c = pool.borrow();
                    c.close();
                } catch (SQLException ignored) {}
            });
            bg.start();
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            assertTrue(bg.isAlive());
            only.close();
            try { bg.join(2000); } catch (InterruptedException ignored) {}
            assertFalse(bg.isAlive());
        }
    }

    @Test
    void rejectsInvalidCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConnectionPool(TestDatabaseConfig.URL,
                        TestDatabaseConfig.USER, TestDatabaseConfig.PASSWORD, 0));
    }
}
