package com.yourname.warehouse.test;

import com.yourname.warehouse.entity.*;
import com.yourname.warehouse.dal.*;
import com.yourname.warehouse.source.*;
import com.yourname.warehouse.service.*;
import java.util.*;

public class ServiceTest {
    public static void main(String[] args) {
        System.out.println("Starting Service Layer Unit Tests...");
        int passed = 0;
        int total = 0;

        try {
            // Setup Environment
            DaoFactory.init(Map.of(
                Laptop.class, new ApplianceDaoImpl<>(new LaptopCsvSourceImpl("Laptops.csv")),
                Oven.class, new ApplianceDaoImpl<>(new OvenCsvSourceImpl("Ovens.csv"))
            ));
            ServiceFactory.init(new ApplianceServiceImpl());
            ApplianceService service = ServiceFactory.getInstance();

            // Test 1: Verify Laptops are loaded
            total++;
            List<Laptop> laptops = service.findLaptops();
            if (laptops.size() == 3) {
                System.out.println("[PASS] Test 1: Laptop count is 3");
                passed++;
            } else {
                System.out.println("[FAIL] Test 1: Expected 3 laptops, found " + laptops.size());
            }

            // Test 2: Verify Cheapest Logic
            total++;
            Appliance<?> cheapest = service.findCheapest();
            if (cheapest != null && "Bosch Oven".equals(cheapest.getName()) && cheapest.getPrice() == 650) {
                System.out.println("[PASS] Test 2: Cheapest item logic correct (Bosch Oven)");
                passed++;
            } else {
                System.out.println("[FAIL] Test 2: Cheapest item logic returned " + (cheapest != null ? cheapest.getName() : "null"));
            }

            // Test 3: Verify Exception Handling
            // (Simulated by passing a non-existent file in a new service instance if needed)

        } catch (Exception e) {
            System.err.println("[ERROR] Testing suite crashed: " + e.getMessage());
        }

        System.out.println("\nTests Completed: " + passed + "/" + total + " Passed.");
        if (passed < total) System.exit(1);
    }
}