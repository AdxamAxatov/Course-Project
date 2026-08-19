package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.service.ApplianceService;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.service.ServiceFactory;

public class CostCommand implements Command {

    @Override
    public Response execute(String[] args) {
        if (args.length < 2) {
            return new ResponseImpl("Usage: cost [laptops|ovens|all]", false, false);
        }

        String target = args[1].toLowerCase();

        if (!target.equals("laptops") && !target.equals("ovens") && !target.equals("all")) {
            return new ResponseImpl("Unknown entity: " + target, false, false);
        }

        try {
            ApplianceService service = ServiceFactory.getInstance();

            long totalCost = switch (target) {
                case "laptops" -> service.calculateLaptopsCost();
                case "ovens" -> service.calculateOvensCost();
                default -> service.calculateTotalCost();
            };

            return new ResponseImpl("Total inventory value for " + target + ": $" + totalCost);
        } catch (ServiceException e) {
            return new ResponseImpl("Error calculating cost: " + e.getMessage(), false, false);
        }
    }
}
