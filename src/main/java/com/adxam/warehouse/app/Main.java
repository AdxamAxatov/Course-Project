package com.adxam.warehouse.app;

import com.adxam.warehouse.view.*;
import com.adxam.warehouse.config.*;

public class Main {
    public static void main(String[] args) {
        String propertiesName = args.length == 0 ? "app" : args[0];
        
        View view = new ConsoleViewImpl(); 
        
        try {
            Config config = new PropertiesConfigImpl(propertiesName);
            config.init(); 
            
            view = ViewFactory.getInstance();
            view.start();
            
        } catch (RuntimeException e) {
            System.err.println("Startup Error Log: " + e.getMessage());
            view.crash(); 
        }
    }
}
