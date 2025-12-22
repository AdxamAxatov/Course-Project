package com.adxam.warehouse.view;

import com.adxam.warehouse.controller.Controller;
import com.adxam.warehouse.controller.ControllerFactory;
import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.RequestImpl;
import com.adxam.warehouse.controller.Response;
import java.util.Scanner;

public class ConsoleViewImpl implements View {
    @Override
    public void start() {
        Controller controller = ControllerFactory.getInstance();
        
        try (Scanner scanner = new Scanner(System.in)) {
            Response response = null; 
            
            boolean keepRunning = true;
            while (keepRunning) {
                System.out.print("> ");
                String line = scanner.nextLine();
                
                if (line.isBlank()) continue;

                Request request = new RequestImpl(line);
                response = controller.execute(request);
                
                System.out.println(response.responseString());
                
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
