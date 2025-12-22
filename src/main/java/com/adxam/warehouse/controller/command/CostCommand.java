package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.*;
import com.adxam.warehouse.service.*;
import com.adxam.warehouse.entity.Appliance;
import com.adxam.warehouse.entity.Range;
import java.util.List;

public class CostCommand implements Command {
    @Override
    public Response execute(String[] args) {
        try {
            ApplianceService service = ServiceFactory.getInstance();
            List<? extends Appliance<?>> items;
            
            if (args.length < 2) return new ResponseImpl("Usage: cost [laptops|ovens|all]", false, false);
            
            String target = args[1].toLowerCase();
            items = switch (target) {
                case "laptops" -> service.findLaptops();
                case "ovens" -> service.findOvens();
                case "all" -> service.findByPrice(new Range<>(0L, Long.MAX_VALUE));
                default -> null;
            };

            if (items == null) return new ResponseImpl("Unknown entity: " + target, false, false);

            long totalCost = items.stream()
                    .mapToLong(item -> item.getPrice() * item.getQuantity())
                    .sum();

            return new ResponseImpl("Total inventory value for " + target + ": $" + totalCost);
        } catch (Exception e) {
            return new ResponseImpl("Error calculating cost: " + e.getMessage(), false, false);
        }
    }
}
