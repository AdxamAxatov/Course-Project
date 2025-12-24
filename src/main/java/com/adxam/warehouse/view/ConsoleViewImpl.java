package com.adxam.warehouse.view;

import com.adxam.warehouse.controller.Controller;
import com.adxam.warehouse.controller.ControllerFactory;
import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.RequestImpl;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.entity.Appliance;

import java.util.List;
import java.util.Scanner;

public class ConsoleViewImpl implements View {

    @Override
    public void start() {
        printHeader();
        printMenu();

        Controller controller = ControllerFactory.getInstance();
        if (controller == null) {
            throw new IllegalStateException("Controller is not initialized. Check configuration.");
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();
                if (line == null) line = "";
                line = line.trim();

                if (line.isEmpty()) {
                    System.out.println("Type 'help' to see available commands.");
                    continue;
                }

                Request request = new RequestImpl(line);
                Response response = controller.execute(request);

                System.out.println(response.responseString());

                if (response.isOut()) {
                    break;
                }
            }
        }
    }

    @Override
    public void crash() {
        System.out.println("Sorry, something went wrong...");
    }


    public static void printHeader() {
        System.out.println("========================================");
        System.out.println(" HOUSEHOLD APPLIANCES WAREHOUSE SYSTEM ");
        System.out.println(" Version: 1.0");
        System.out.println(" Created: 2025");
        System.out.println(" Developer: Adkham Akhatov (Adkham_Akhatov@student.itpu.uz)");
        System.out.println("========================================");
    }

    public static void printMenu() {
        System.out.println("Available commands:");
        System.out.println("  help");
        System.out.println("  find laptops [key=value ...]");
        System.out.println("  find ovens [key=value ...]");
        System.out.println("  find all [key=value ...]");
        System.out.println("  cost laptops [key=value ...]");
        System.out.println("  exit");
    }

    public static void printAppliances(List<? extends Appliance> appliances) {
        if (appliances == null || appliances.isEmpty()) {
            System.out.println("No matching products were found.");
            return;
        }
        for (Appliance a : appliances) {
            System.out.println(a);
        }
    }

    public static void printError(String message) {
        System.out.println("Error: " + message);
    }

    public static void printTotalCost(double cost) {
        System.out.println("========================================");
        System.out.println("TOTAL INVENTORY VALUE");
        System.out.println("----------------------------------------");
        System.out.println("Total value: $" + cost);
        System.out.println("========================================");
    }
}
