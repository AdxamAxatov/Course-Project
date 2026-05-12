package com.adxam.warehouse.dal.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public class Database implements AutoCloseable {
    private final ConnectionPool pool;

    public Database(ConnectionPool pool) {
        this.pool = pool;
    }

    public ConnectionPool pool() { return pool; }

    public <T> T inTransaction(SqlFunction<Connection, T> work) throws SQLException {
        try (Connection c = pool.borrow()) {
            boolean previousAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                T result = work.apply(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public <T> T query(Function<Connection, T> work) throws SQLException {
        try (Connection c = pool.borrow()) {
            return work.apply(c);
        }
    }

    @Override
    public void close() {
        pool.close();
    }

    @FunctionalInterface
    public interface SqlFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
