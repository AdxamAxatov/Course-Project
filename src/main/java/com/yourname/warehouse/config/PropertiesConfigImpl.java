package com.yourname.warehouse.config;

import com.yourname.warehouse.view.*;
import com.yourname.warehouse.controller.*;
import com.yourname.warehouse.service.*;
import com.yourname.warehouse.dal.*;
import com.yourname.warehouse.source.*;
import com.yourname.warehouse.entity.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class PropertiesConfigImpl implements Config {
    private final String propertiesName;

    public PropertiesConfigImpl(String propertiesName) {
        this.propertiesName = propertiesName;
    }

    @Override
    public void init() {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(propertiesName + ".properties")) {
            
            if (is == null) throw new IOException("Properties file not found: " + propertiesName);
            
            Properties props = new Properties();
            props.load(is);

            // Initialize all layers in order
            initDao(props);
            initService(props);
            initController(props);
            initView(props);

        } catch (IOException e) {
            throw new RuntimeException("Configuration failed", e);
        }
    }

    private void initView(Properties props) {
        String mode = props.getProperty("view");
        View view = switch (mode) {
            case "console" -> new ConsoleViewImpl();
            default -> throw new IllegalStateException("Unknown view: " + mode);
        };
        ViewFactory.init(view);
    }

    private void initController(Properties props) {
        if ("simple".equals(props.getProperty("controller"))) {
            ControllerFactory.init(new ControllerImpl());
        }
    }

    private void initService(Properties props) {
        if ("simple".equals(props.getProperty("service"))) {
            ServiceFactory.init(new ApplianceServiceImpl());
        }
    }

    private void initDao(Properties props) {
        String laptopFile = props.getProperty("source.laptop") + ".csv";
        String ovenFile = props.getProperty("source.oven") + ".csv";
        
        if ("straight".equals(props.getProperty("dao"))) {
            DaoFactory.init(Map.of(
                Laptop.class, new ApplianceDaoImpl<>(new LaptopCsvSourceImpl(laptopFile)),
                Oven.class, new ApplianceDaoImpl<>(new OvenCsvSourceImpl(ovenFile))
            ));
        }
    }
}