package com.yourname.warehouse.view;

import com.yourname.warehouse.controller.Controller;
import com.yourname.warehouse.controller.ControllerFactory;
import com.yourname.warehouse.controller.Request;
import com.yourname.warehouse.controller.RequestImpl;
import com.yourname.warehouse.controller.Response;
import java.util.Scanner;

public class ConsoleViewImpl implements View {
    @Override
    public void start() {
        Controller controller = ControllerFactory.getInstance();
        
        try (Scanner scanner = new Scanner(System.in)) {
            // Initialize to null to satisfy the compiler
            Response response = null; 
            
            boolean keepRunning = true;
            while (keepRunning) {
                System.out.print("> ");
                String line = scanner.nextLine();
                
                if (line.isBlank()) continue;

                Request request = new RequestImpl(line);
                response = controller.execute(request);
                
                System.out.println(response.responseString());
                
                // Use the flag from the response to decide when to stop
                if (response.isOut()) {
                    keepRunning = false;
                }
            }
        } catch (Exception e) {
            crash();
        }
    }

    @Override
    public void crash() {
        System.out.println("Sorry, something went wrong...");
    }
}