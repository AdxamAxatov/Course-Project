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

public class FindCommand implements Command {

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
                case "all" -> service.findByPrice(parsePriceRange(args));
                default -> throw new ValidationException("Unknown target: " + target);
            };
            return new ResponseImpl(format(items));
        } catch (ServiceException e) {
            return new ResponseImpl("Find failed: " + e.getMessage(), false, false);
        }
    }

    private Range<Long> parsePriceRange(CommandArgs args) {
        if (!args.hasMore()) return new Range<>(0L, Long.MAX_VALUE);
        String token = args.requireString("price=min;max");
        if (!token.startsWith("price=")) {
            throw new ValidationException("Expected 'price=min;max', got: " + token);
        }
        String[] parts = token.substring(6).split(";");
        if (parts.length != 2) {
            throw new ValidationException("Price range must be 'min;max'");
        }
        try {
            return new Range<>(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
        } catch (NumberFormatException e) {
            throw new ValidationException("Price bounds must be integers");
        }
    }

    private String format(List<? extends Appliance<?>> items) {
        if (items.isEmpty()) return "No items found.";
        StringBuilder sb = new StringBuilder();
        for (Appliance<?> a : items) {
            sb.append(a).append(System.lineSeparator());
        }
        return sb.toString().stripTrailing();
    }
}
