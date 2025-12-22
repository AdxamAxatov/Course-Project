package com.yourname.warehouse.controller.command;

import com.yourname.warehouse.controller.Response;
import com.yourname.warehouse.controller.ResponseImpl;
import com.yourname.warehouse.service.ApplianceService;
import com.yourname.warehouse.service.ServiceFactory;
import com.yourname.warehouse.entity.Range;

public class FindCommand implements Command {

    @Override
    public Response execute(String[] args) {
        try {
            ApplianceService service = ServiceFactory.getInstance();
            
            // Validation: Ensure there's at least one argument (find <entity>)
            if (args.length < 2) {
                return new ResponseImpl("Format: find [laptops|ovens|all] [price=min;max]", false, false);
            }

            String target = args[1].toLowerCase();

            // 1. Handle "find all"
            if (target.equals("all")) {
                // Case: find all price=100;500
                if (args.length == 3 && args[2].startsWith("price=")) {
                    String[] rangeParts = args[2].substring(6).split(";");
                    Range<Long> range = new Range<>(Long.parseLong(rangeParts[0]), Long.parseLong(rangeParts[1]));
                    return new ResponseImpl(service.findByPrice(range).toString());
                } 
                // Case: find all (Shows everything)
                else if (args.length == 2) {
                    return new ResponseImpl(service.findByPrice(new Range<>(0L, Long.MAX_VALUE)).toString());
                }
            }
            
            // 2. Handle specific entities
            if (target.startsWith("laptop")) {
                return new ResponseImpl(service.findLaptops().toString());
            }
            if (target.startsWith("oven")) {
                return new ResponseImpl(service.findOvens().toString());
            }

            throw new IllegalArgumentException("Invalid command format");
            
        } catch (Exception e) {
            // Translates any errors into a clean response for the View
            return new ResponseImpl(e.getMessage(), false, false);
        }
    }
}