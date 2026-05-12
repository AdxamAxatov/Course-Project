package com.adxam.warehouse.app;

import com.adxam.warehouse.config.PropertiesConfigImpl;
import com.adxam.warehouse.dal.db.Database;
import com.adxam.warehouse.net.SocketServer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {
    private static final Logger LOG = Logger.getLogger(ServerMain.class.getName());

    public static void main(String[] args) {
        String configName = args.length > 0 ? args[0] : "app";

        PropertiesConfigImpl config = new PropertiesConfigImpl(configName);
        try {
            config.init();
        } catch (RuntimeException e) {
            System.err.println("Startup error: " + e.getMessage());
            System.exit(1);
            return;
        }

        Database db = config.database();
        SocketServer server = new SocketServer(config.serverPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook invoked");
            server.close();
            if (db != null) db.close();
        }, "shutdown"));

        try {
            server.start();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e, () -> "Server failed");
            System.exit(2);
        }
    }
}
