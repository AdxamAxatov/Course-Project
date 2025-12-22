package com.yourname.warehouse.controller.command;
import com.yourname.warehouse.controller.*;
public class WrongCommand implements Command {
    @Override
    public Response execute(String[] args) {
        return new ResponseImpl("Wrong command", false, false);
    }
}