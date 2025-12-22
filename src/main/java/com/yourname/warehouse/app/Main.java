package com.yourname.warehouse.app;

import com.yourname.warehouse.view.*;
import com.yourname.warehouse.config.*;

public class Main {
    public static void main(String[] args) {
        // Use "app" as default properties name, or take from command line
        String propertiesName = args.length == 0 ? "app" : args[0];
        
        // Default view for error reporting
        View view = new ConsoleViewImpl(); 
        
        try {
            Config config = new PropertiesConfigImpl(propertiesName);
            config.init(); // Initialize factories via properties
            
            // Get the configured view and start
            view = ViewFactory.getInstance();
            view.start();
            
        } catch (RuntimeException e) {
            // Log for developer and notify user
            System.err.println("Startup Error Log: " + e.getMessage());
            view.crash(); 
        }
    }
}