package com.adxam.warehouse.app;

import com.adxam.warehouse.config.Config;
import com.adxam.warehouse.config.PropertiesConfigImpl;
import com.adxam.warehouse.view.ConsoleViewImpl;
import com.adxam.warehouse.view.View;
import com.adxam.warehouse.view.ViewFactory;

public class Main {

    public static void main(String[] args) {
        String propertiesName = args.length == 0 ? "app" : args[0];

        View view = new ConsoleViewImpl();

        try {
            Config config = new PropertiesConfigImpl(propertiesName);
            config.init();
        } catch (RuntimeException e) {
            System.err.println("Configuration error: " + e.getMessage());
            view.crash();
            return;
        }

        View configured = ViewFactory.getInstance();
        if (configured == null) {
            System.err.println("Configuration error: no view was created for '" + propertiesName + "'");
            view.crash();
            return;
        }

        try {
            configured.start();
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
            configured.crash();
        }
    }
}
