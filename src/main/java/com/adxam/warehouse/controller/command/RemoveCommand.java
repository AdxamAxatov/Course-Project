package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.service.ApplianceService;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.service.ServiceFactory;
import com.adxam.warehouse.util.CommandArgs;
import com.adxam.warehouse.util.ValidationException;

import java.util.EnumSet;
import java.util.Set;

public class RemoveCommand implements Command {

    @Override
    public Set<Role> allowedRoles() {
        return EnumSet.of(Role.ADMIN, Role.USER);
    }

    @Override
    public Response execute(Request request) {
        CommandArgs args = new CommandArgs(request.tokens(), 1);
        String kind = args.requireString("kind (laptop|oven)").toLowerCase();
        long id = args.requireLong("id");
        ApplianceService service = ServiceFactory.getInstance();
        try {
            boolean removed = switch (kind) {
                case "laptop" -> service.removeLaptop(id);
                case "oven" -> service.removeOven(id);
                default -> throw new ValidationException("Unknown kind: " + kind);
            };
            return new ResponseImpl(removed
                    ? "Removed " + kind + " id=" + id
                    : "No " + kind + " found with id=" + id);
        } catch (ServiceException e) {
            return new ResponseImpl("Remove failed: " + e.getMessage(), false, false);
        }
    }
}
