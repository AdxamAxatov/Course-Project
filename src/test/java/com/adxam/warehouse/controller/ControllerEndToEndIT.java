package com.adxam.warehouse.controller;

import com.adxam.warehouse.dal.ApplianceDao;
import com.adxam.warehouse.dal.DaoFactory;
import com.adxam.warehouse.dal.UserDaoFactory;
import com.adxam.warehouse.dal.db.ConnectionPool;
import com.adxam.warehouse.dal.db.Database;
import com.adxam.warehouse.dal.db.JdbcLaptopDao;
import com.adxam.warehouse.dal.db.JdbcOvenDao;
import com.adxam.warehouse.dal.db.JdbcUserDao;
import com.adxam.warehouse.dal.db.SchemaInitializer;
import com.adxam.warehouse.entity.Appliance;
import com.adxam.warehouse.entity.Laptop;
import com.adxam.warehouse.entity.Oven;
import com.adxam.warehouse.service.ApplianceServiceImpl;
import com.adxam.warehouse.service.AuthServiceFactory;
import com.adxam.warehouse.service.AuthServiceImpl;
import com.adxam.warehouse.service.ServiceFactory;
import com.adxam.warehouse.service.UserServiceFactory;
import com.adxam.warehouse.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ControllerEndToEndIT {

    private static ControllerImpl controller;
    private static Session session;

    @BeforeAll
    static void bootstrap() throws SQLException {
        String url = "jdbc:h2:mem:e2e-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        ConnectionPool pool = new ConnectionPool(url, "sa", "", 3);
        Database db = new Database(pool);
        new SchemaInitializer(pool, "admin", "admin").run();

        JdbcUserDao userDao = new JdbcUserDao(db);
        UserDaoFactory.init(userDao);
        AuthServiceFactory.init(new AuthServiceImpl(userDao));
        UserServiceFactory.init(new UserServiceImpl(userDao));

        Map<Class<? extends Appliance<?>>, ApplianceDao<?>> daos = new HashMap<>();
        daos.put(Laptop.class, new JdbcLaptopDao(db));
        daos.put(Oven.class, new JdbcOvenDao(db));
        DaoFactory.init(daos);

        ServiceFactory.init(new ApplianceServiceImpl());
        ControllerFactory.init(new ControllerImpl());

        controller = new ControllerImpl();
        session = new Session();
    }

    private Response run(String line) {
        return controller.execute(RequestImpl.parse(line, session));
    }

    @Test
    @Order(1)
    void unauthenticatedCommandsAreRejected() {
        Response r = run("find laptops");
        assertFalse(r.isOk());
        assertTrue(r.responseString().toLowerCase().contains("login"));
    }

    @Test
    @Order(2)
    void publicCommandsWorkWithoutAuth() {
        assertTrue(run("help").isOk());
        assertTrue(run("visit").isOk());
        session.logout();
    }

    @Test
    @Order(3)
    void seededAdminCanLogIn() {
        Response r = run("login admin admin");
        assertTrue(r.isOk(), r.responseString());
        assertTrue(session.isAuthenticated());
    }

    @Test
    @Order(4)
    void adminCanAddAndFindLaptop() {
        Response add = run("add laptop X1 1.1 2200 3 Linux Intel-i7 450");
        assertTrue(add.isOk(), add.responseString());
        Response find = run("find laptops");
        assertTrue(find.responseString().contains("X1"));
    }

    @Test
    @Order(5)
    void adminCanManageUsers() {
        Response add = run("users add bob pw USER");
        assertTrue(add.isOk(), add.responseString());

        Response list = run("users list");
        assertTrue(list.responseString().contains("bob"));
    }

    @Test
    @Order(6)
    void visitorCannotAdd() {
        session.logout();
        session.enterAsVisitor();
        Response r = run("add laptop Y 1 100 1 Linux Intel 100");
        assertFalse(r.isOk());
        assertTrue(r.responseString().toLowerCase().contains("permission"));
    }

    @Test
    @Order(7)
    void visitorCanRead() {
        Response find = run("find laptops");
        assertTrue(find.isOk(), find.responseString());
    }

    @Test
    @Order(8)
    void cheapestReturnsLowestPricedItem() {
        Response r = run("cheapest");
        assertTrue(r.isOk(), r.responseString());
        assertTrue(r.responseString().contains("Cheapest:"));
    }

    @Test
    @Order(9)
    void exitFlagsTermination() {
        Response r = run("exit");
        assertTrue(r.isOut());
    }
}
