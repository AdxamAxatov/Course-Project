package com.adxam.warehouse.dal.db;

import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcUserDaoIT {

    private static ConnectionPool pool;
    private static Database db;
    private static JdbcUserDao dao;

    @BeforeAll
    static void setUp() throws SQLException {
        TestDatabaseConfig.ensureDatabaseExists();
        pool = new ConnectionPool(TestDatabaseConfig.URL,
                TestDatabaseConfig.USER, TestDatabaseConfig.PASSWORD, 2);
        db = new Database(pool);
        new SchemaInitializer(pool, "ignored", "ignored").createSchema();
        dao = new JdbcUserDao(db);
    }

    @AfterAll
    static void tearDown() {
        db.close();
    }

    @BeforeEach
    void truncate() throws SQLException {
        try (Connection c = pool.borrow(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM users");
        }
    }

    @Test
    void insertAssignsGeneratedId() throws Exception {
        User saved = dao.insert(new User()
                .setUsername("alice")
                .setPasswordHash("hash")
                .setSalt("salt")
                .setRole(Role.USER));
        assertTrue(saved.getId() > 0);
    }

    @Test
    void findByUsernameReturnsInserted() throws Exception {
        dao.insert(new User().setUsername("bob").setPasswordHash("h").setSalt("s").setRole(Role.ADMIN));
        Optional<User> found = dao.findByUsername("bob");
        assertTrue(found.isPresent());
        assertEquals(Role.ADMIN, found.get().getRole());
    }

    @Test
    void findByUsernameEmptyWhenAbsent() throws Exception {
        assertTrue(dao.findByUsername("ghost").isEmpty());
    }

    @Test
    void findAllReturnsAllUsers() throws Exception {
        dao.insert(new User().setUsername("a").setPasswordHash("x").setSalt("y").setRole(Role.USER));
        dao.insert(new User().setUsername("b").setPasswordHash("x").setSalt("y").setRole(Role.USER));
        List<User> users = dao.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void deleteByIdReturnsTrueWhenRemoved() throws Exception {
        User saved = dao.insert(new User().setUsername("z").setPasswordHash("x").setSalt("y").setRole(Role.USER));
        assertTrue(dao.deleteById(saved.getId()));
        assertTrue(dao.findById(saved.getId()).isEmpty());
    }

    @Test
    void deleteByIdReturnsFalseWhenMissing() throws Exception {
        assertFalse(dao.deleteById(999_999));
    }
}
