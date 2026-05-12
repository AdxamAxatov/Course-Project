package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.entity.Appliance;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.service.ApplianceService;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.service.ServiceFactory;

import java.util.EnumSet;
import java.util.Set;

public class CheapestCommand implements Command {

    @Override
    public Set<Role> allowedRoles() {
        return EnumSet.of(Role.ADMIN, Role.USER, Role.VISITOR);
    }

    @Override
    public Response execute(Request request) {
        ApplianceService service = ServiceFactory.getInstance();
        try {
            Appliance<?> cheapest = service.findCheapest();
            if (cheapest == null) return new ResponseImpl("No appliances in stock.");
            return new ResponseImpl("Cheapest: " + cheapest);
        } catch (ServiceException e) {
            return new ResponseImpl("Cheapest failed: " + e.getMessage(), false, false);
        }
    }
}
