package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.entity.Laptop;
import com.adxam.warehouse.entity.Oven;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.service.ApplianceService;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.service.ServiceFactory;
import com.adxam.warehouse.util.CommandArgs;
import com.adxam.warehouse.util.ValidationException;

import java.util.EnumSet;
import java.util.Set;

public class AddCommand implements Command {

    @Override
    public Set<Role> allowedRoles() {
        return EnumSet.of(Role.ADMIN, Role.USER);
    }

    @Override
    public Response execute(Request request) {
        CommandArgs args = new CommandArgs(request.tokens(), 1);
        String kind = args.requireString("kind (laptop|oven)").toLowerCase();
        ApplianceService service = ServiceFactory.getInstance();
        try {
            return switch (kind) {
                case "laptop" -> addLaptop(args, service);
                case "oven" -> addOven(args, service);
                default -> throw new ValidationException("Unknown kind: " + kind);
            };
        } catch (ServiceException e) {
            return new ResponseImpl("Add failed: " + e.getMessage(), false, false);
        }
    }

    private Response addLaptop(CommandArgs args, ApplianceService service) throws ServiceException {
        Laptop laptop = new Laptop()
                .setName(args.requireString("name"))
                .setWeight(args.requireDouble("weight"))
                .setPrice(args.requireLong("price"))
                .setQuantity(args.requireInt("quantity"))
                .setOs(args.requireString("os"))
                .setCpu(args.requireString("cpu"))
                .setBatteryCapacity(args.requireInt("battery"));
        Laptop saved = service.addLaptop(laptop);
        return new ResponseImpl("Added: " + saved);
    }

    private Response addOven(CommandArgs args, ApplianceService service) throws ServiceException {
        Oven oven = new Oven()
                .setName(args.requireString("name"))
                .setWeight(args.requireDouble("weight"))
                .setPrice(args.requireLong("price"))
                .setQuantity(args.requireInt("quantity"))
                .setPowerConsumption(args.requireInt("power"))
                .setCapacity(args.requireDouble("capacity"));
        Oven saved = service.addOven(oven);
        return new ResponseImpl("Added: " + saved);
    }
}
