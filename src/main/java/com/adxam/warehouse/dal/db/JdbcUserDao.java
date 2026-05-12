package com.adxam.warehouse.dal.db;

import com.adxam.warehouse.dal.DaoException;
import com.adxam.warehouse.dal.UserDao;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private static final String COLUMNS = "id, username, password_hash, salt, role";
    private final Database db;

    public JdbcUserDao(Database db) {
        this.db = db;
    }

    @Override
    public Optional<User> findByUsername(String username) throws DaoException {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE username = ?", ps -> ps.setString(1, username));
    }

    @Override
    public Optional<User> findById(long id) throws DaoException {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", ps -> ps.setLong(1, id));
    }

    @Override
    public List<User> findAll() throws DaoException {
        try {
            return db.query(c -> {
                try (PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM users ORDER BY id");
                     ResultSet rs = ps.executeQuery()) {
                    List<User> users = new ArrayList<>();
                    while (rs.next()) users.add(map(rs));
                    return users;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException | SQLException e) {
            throw new DaoException(unwrap(e));
        }
    }

    @Override
    public User insert(User user) throws DaoException {
        String sql = "INSERT INTO users (username, password_hash, salt, role) VALUES (?, ?, ?, ?)";
        try {
            return db.inTransaction(c -> {
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getSalt());
                    ps.setString(4, user.getRole().name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) user.setId(keys.getLong(1));
                    }
                }
                return user;
            });
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public boolean deleteById(long id) throws DaoException {
        try {
            return db.inTransaction(c -> {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    ps.setLong(1, id);
                    return ps.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private Optional<User> findOne(String sql, SqlBinder binder) throws DaoException {
        try {
            return db.query(c -> {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    binder.bind(ps);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? Optional.of(map(rs)) : Optional.<User>empty();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException | SQLException e) {
            throw new DaoException(unwrap(e));
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User()
                .setId(rs.getLong("id"))
                .setUsername(rs.getString("username"))
                .setPasswordHash(rs.getString("password_hash"))
                .setSalt(rs.getString("salt"))
                .setRole(Role.parse(rs.getString("role")));
    }

    private static Throwable unwrap(Throwable t) {
        return (t instanceof RuntimeException re && re.getCause() != null) ? re.getCause() : t;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
