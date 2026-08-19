package com.adxam.warehouse.view;

import com.adxam.warehouse.controller.Controller;
import com.adxam.warehouse.controller.RequestImpl;
import com.adxam.warehouse.controller.Response;

import java.util.Scanner;

public class ConsoleViewImpl extends AbstractView {

    @Override
    public void start() {
        Controller controller = controller();

        printHeader();
        System.out.println(controller.execute(new RequestImpl("help")).responseString());

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");

                if (!scanner.hasNextLine()) {
                    System.out.println();
                    break;
                }

                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    System.out.println("Type 'help' to see the available commands.");
                    continue;
                }

                Response response = controller.execute(new RequestImpl(line));
                System.out.println(response.responseString());

                if (response.isOut()) {
                    break;
                }
            }
        }
    }

    private static void printHeader() {
        System.out.println("========================================");
        System.out.println(" HOUSEHOLD APPLIANCES WAREHOUSE SYSTEM ");
        System.out.println(" Version: 1.0");
        System.out.println(" Created: 2025");
        System.out.println(" Developer: Adkham Akhatov (Adkham_Akhatov@student.itpu.uz)");
        System.out.println("========================================");
    }
}
