package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.service.ApplianceService;
import com.adxam.warehouse.service.ServiceFactory;
import com.adxam.warehouse.entity.Range;

public class FindCommand implements Command {

    @Override
    public Response execute(String[] args) {
        try {
            ApplianceService service = ServiceFactory.getInstance();
            
            if (args.length < 2) {
                return new ResponseImpl("Format: find [laptops|ovens|all] [price=min;max]", false, false);
            }

            String target = args[1].toLowerCase();

            if (target.equals("all")) {
                if (args.length == 3 && args[2].startsWith("price=")) {
                    String[] rangeParts = args[2].substring(6).split(";");
                    Range<Long> range = new Range<>(Long.parseLong(rangeParts[0]), Long.parseLong(rangeParts[1]));
                    return new ResponseImpl(service.findByPrice(range).toString());
                } 
                else if (args.length == 2) {
                    return new ResponseImpl(service.findByPrice(new Range<>(0L, Long.MAX_VALUE)).toString());
                }
            }
            
            if (target.startsWith("laptop")) {
                return new ResponseImpl(service.findLaptops().toString());
            }
            if (target.startsWith("oven")) {
                return new ResponseImpl(service.findOvens().toString());
            }

            throw new IllegalArgumentException("Invalid command format");
            
        } catch (Exception e) {
            return new ResponseImpl(e.getMessage(), false, false);
        }
    }
}
