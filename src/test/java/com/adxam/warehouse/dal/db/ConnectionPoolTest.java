package com.adxam.warehouse.dal.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionPoolTest {

    @Test
    void borrowedConnectionIsReusedAfterClose() throws SQLException {
        String url = "jdbc:h2:mem:pool-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        try (ConnectionPool pool = new ConnectionPool(url, "sa", "", 1)) {
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
        String url = "jdbc:h2:mem:pool2-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        try (ConnectionPool pool = new ConnectionPool(url, "sa", "", 1)) {
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
                () -> new ConnectionPool("jdbc:h2:mem:x", "sa", "", 0));
    }
}
