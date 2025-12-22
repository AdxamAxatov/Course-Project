package com.adxam.warehouse.controller.command;
import com.adxam.warehouse.controller.*;
public class ExitCommand implements Command {
    @Override
    public Response execute(String[] args) {
        return new ResponseImpl("Great job!", true, true);
    }
}
