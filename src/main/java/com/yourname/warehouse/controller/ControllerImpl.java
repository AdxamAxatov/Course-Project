package com.yourname.warehouse.controller;
import com.yourname.warehouse.controller.command.*;
public class ControllerImpl implements Controller {
    @Override
    public Response execute(Request request) {
        String[] parts = request.requestString().trim().split("\\s+");
        Command command = CommandProvider.getCommand(parts[0]);
        return command.execute(parts);
    }
}