package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;

public class ExitCommand implements Command {

    @Override
    public boolean requiresAuth() {
        return false;
    }

    @Override
    public Response execute(Request request) {
        return new ResponseImpl("Goodbye!", true, true);
    }
}
