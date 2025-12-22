package com.adxam.warehouse.controller.command;
import com.adxam.warehouse.controller.*;
public class WrongCommand implements Command {
    @Override
    public Response execute(String[] args) {
        return new ResponseImpl("Wrong command", false, false);
    }
}
