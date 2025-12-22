package com.yourname.warehouse.controller.command;
import com.yourname.warehouse.controller.*;
public class ExitCommand implements Command {
    @Override
    public Response execute(String[] args) {
        return new ResponseImpl("Great job!", true, true);
    }
}