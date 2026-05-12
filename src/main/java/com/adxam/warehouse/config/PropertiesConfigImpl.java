package com.adxam.warehouse.config;

import com.adxam.warehouse.controller.ControllerFactory;
import com.adxam.warehouse.controller.ControllerImpl;
import com.adxam.warehouse.dal.ApplianceDao;
import com.adxam.warehouse.dal.ApplianceDaoImpl;
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
import com.adxam.warehouse.source.LaptopCsvSourceImpl;
import com.adxam.warehouse.source.OvenCsvSourceImpl;
import com.adxam.warehouse.util.Logging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertiesConfigImpl implements Config {
    private static final Logger LOG = Logger.getLogger(PropertiesConfigImpl.class.getName());

    private final String propertiesName;
    private Database database;
    private int serverPort;

    public PropertiesConfigImpl(String propertiesName) {
        this.propertiesName = propertiesName;
    }

    @Override
    public void init() {
        Properties props = loadProps();
        Logging.configure(props.getProperty("log.level", "INFO"));
        LOG.info(() -> "Loaded properties: " + propertiesName);

        String storage = props.getProperty("storage", "jdbc").toLowerCase();
        switch (storage) {
            case "jdbc" -> initJdbc(props);
            case "csv" -> initCsv(props);
            default -> throw new IllegalStateException("Unknown storage: " + storage);
        }

        ServiceFactory.init(new ApplianceServiceImpl());
        ControllerFactory.init(new ControllerImpl());

        this.serverPort = Integer.parseInt(props.getProperty("server.port", "7070"));
    }

    public Database database() { return database; }
    public int serverPort() { return serverPort; }

    private Properties loadProps() {
        Properties props = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(propertiesName + ".properties")) {
            if (is == null) throw new IOException("Properties file not found: " + propertiesName);
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Configuration failed: " + e.getMessage(), e);
        }
        return props;
    }

    private void initJdbc(Properties props) {
        String url = props.getProperty("db.url", "jdbc:h2:./data/warehouse;AUTO_SERVER=TRUE");
        String user = props.getProperty("db.user", "sa");
        String password = props.getProperty("db.password", "");
        int poolSize = Integer.parseInt(props.getProperty("db.poolSize", "5"));
        ensureParentDir(url);

        try {
            ConnectionPool pool = new ConnectionPool(url, user, password, poolSize);
            database = new Database(pool);

            String adminUser = props.getProperty("admin.username", "admin");
            String adminPass = props.getProperty("admin.password", "admin");
            new SchemaInitializer(pool, adminUser, adminPass).run();

            JdbcUserDao userDao = new JdbcUserDao(database);
            UserDaoFactory.init(userDao);
            AuthServiceFactory.init(new AuthServiceImpl(userDao));
            UserServiceFactory.init(new UserServiceImpl(userDao));

            Map<Class<? extends Appliance<?>>, ApplianceDao<?>> daos = new HashMap<>();
            daos.put(Laptop.class, new JdbcLaptopDao(database));
            daos.put(Oven.class, new JdbcOvenDao(database));
            DaoFactory.init(daos);
        } catch (SQLException e) {
            throw new RuntimeException("JDBC initialization failed: " + e.getMessage(), e);
        }
    }

    private void initCsv(Properties props) {
        String laptopFile = props.getProperty("source.laptop", "Laptops") + ".csv";
        String ovenFile = props.getProperty("source.oven", "Ovens") + ".csv";
        Map<Class<? extends Appliance<?>>, ApplianceDao<?>> daos = new HashMap<>();
        daos.put(Laptop.class, new ApplianceDaoImpl<>(new LaptopCsvSourceImpl(laptopFile)));
        daos.put(Oven.class, new ApplianceDaoImpl<>(new OvenCsvSourceImpl(ovenFile)));
        DaoFactory.init(daos);
        LOG.warning("CSV storage is read-only; add/remove/users commands will fail. Use storage=jdbc for full functionality.");
    }

    private void ensureParentDir(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:h2:") || jdbcUrl.contains(":mem:")) return;
        String filePart = jdbcUrl.substring("jdbc:h2:".length());
        int sep = filePart.indexOf(';');
        if (sep > 0) filePart = filePart.substring(0, sep);
        Path p = Path.of(filePart).toAbsolutePath();
        Path dir = p.getParent();
        if (dir != null) {
            try { Files.createDirectories(dir); }
            catch (IOException e) { throw new RuntimeException("Cannot create DB directory: " + dir, e); }
        }
    }
}
