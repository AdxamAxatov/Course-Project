package com.adxam.warehouse.dal.db;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ConnectionPool implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(ConnectionPool.class.getName());

    private final BlockingQueue<Connection> available;
    private final List<Connection> all;
    private final String url;
    private final String user;
    private final String password;
    private final int capacity;
    private volatile boolean closed = false;

    public ConnectionPool(String url, String user, String password, int capacity) throws SQLException {
        if (capacity < 1) throw new IllegalArgumentException("Pool capacity must be >= 1");
        this.url = url;
        this.user = user;
        this.password = password;
        this.capacity = capacity;
        this.available = new ArrayBlockingQueue<>(capacity);
        this.all = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            Connection raw = DriverManager.getConnection(url, user, password);
            all.add(raw);
            available.add(wrap(raw));
        }
        LOG.info(() -> "ConnectionPool opened: " + url + " size=" + capacity);
    }

    public Connection borrow() throws SQLException {
        if (closed) throw new SQLException("Pool is closed");
        try {
            Connection c = available.poll(10, TimeUnit.SECONDS);
            if (c == null) throw new SQLException("Timed out waiting for a connection");
            return c;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while borrowing connection", e);
        }
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        available.clear();
        for (Connection c : all) {
            try { c.close(); } catch (SQLException ignored) {}
        }
        LOG.info("ConnectionPool closed");
    }

    public int capacity() { return capacity; }

    private Connection wrap(Connection raw) {
        return (Connection) Proxy.newProxyInstance(
                ConnectionPool.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        if (!closed) available.offer((Connection) proxy);
                        return null;
                    }
                    try {
                        return method.invoke(raw, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
