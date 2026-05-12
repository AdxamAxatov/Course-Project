package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.entity.Appliance;
import com.adxam.warehouse.entity.Range;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.service.ApplianceService;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.service.ServiceFactory;
import com.adxam.warehouse.util.CommandArgs;
import com.adxam.warehouse.util.ValidationException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class CostCommand implements Command {

    @Override
    public Set<Role> allowedRoles() {
        return EnumSet.of(Role.ADMIN, Role.USER, Role.VISITOR);
    }

    @Override
    public Response execute(Request request) {
        CommandArgs args = new CommandArgs(request.tokens(), 1);
        String target = args.requireString("target (laptops|ovens|all)").toLowerCase();
        ApplianceService service = ServiceFactory.getInstance();
        try {
            List<? extends Appliance<?>> items = switch (target) {
                case "laptops" -> service.findLaptops();
                case "ovens" -> service.findOvens();
                case "all" -> service.findByPrice(new Range<>(0L, Long.MAX_VALUE));
                default -> throw new ValidationException("Unknown target: " + target);
            };
            long total = items.stream()
                    .mapToLong(item -> item.getPrice() * item.getQuantity())
                    .sum();
            return new ResponseImpl("Total inventory value for " + target + ": $" + total);
        } catch (ServiceException e) {
            return new ResponseImpl("Cost failed: " + e.getMessage(), false, false);
        }
    }
}
