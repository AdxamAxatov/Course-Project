package com.adxam.warehouse.dal.db;

import com.adxam.warehouse.criteria.LaptopSearchCriteria;
import com.adxam.warehouse.criteria.Parameter;
import com.adxam.warehouse.entity.Laptop;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JdbcLaptopDaoIT {

    private static ConnectionPool pool;
    private static Database db;
    private static JdbcLaptopDao dao;

    @BeforeAll
    static void setUp() throws SQLException {
        String url = "jdbc:h2:mem:laptopdao-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        pool = new ConnectionPool(url, "sa", "", 2);
        db = new Database(pool);
        new SchemaInitializer(pool, "x", "x").createSchema();
        dao = new JdbcLaptopDao(db);
    }

    @AfterAll
    static void tearDown() {
        db.close();
    }

    @BeforeEach
    void truncate() throws SQLException {
        try (Connection c = pool.borrow(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM laptops");
            s.execute("ALTER TABLE laptops ALTER COLUMN id RESTART WITH 1");
        }
    }

    @Test
    void insertedLaptopIsFindable() throws Exception {
        Laptop saved = dao.insert(new Laptop()
                .setName("X1 Carbon")
                .setWeight(1.1)
                .setPrice(2200)
                .setQuantity(3)
                .setOs("Linux")
                .setCpu("Intel i7")
                .setBatteryCapacity(450));
        assertTrue(saved.getId() > 0);

        List<Laptop> all = dao.find(new LaptopSearchCriteria().add(Parameter.any()));
        assertEquals(1, all.size());
        assertEquals("X1 Carbon", all.get(0).getName());
    }

    @Test
    void criteriaFiltersByPrice() throws Exception {
        dao.insert(new Laptop().setName("cheap").setWeight(1).setPrice(500).setQuantity(1)
                .setOs("L").setCpu("c").setBatteryCapacity(100));
        dao.insert(new Laptop().setName("pricey").setWeight(1).setPrice(3000).setQuantity(1)
                .setOs("L").setCpu("c").setBatteryCapacity(100));

        List<Laptop> result = dao.find(new LaptopSearchCriteria().add(l -> l.getPrice() < 1000));
        assertEquals(1, result.size());
        assertEquals("cheap", result.get(0).getName());
    }

    @Test
    void deleteByIdRemoves() throws Exception {
        Laptop saved = dao.insert(new Laptop().setName("X").setWeight(1).setPrice(500).setQuantity(1)
                .setOs("L").setCpu("c").setBatteryCapacity(100));
        assertTrue(dao.deleteById(saved.getId()));
        assertEquals(0, dao.find(new LaptopSearchCriteria().add(Parameter.any())).size());
    }
}
