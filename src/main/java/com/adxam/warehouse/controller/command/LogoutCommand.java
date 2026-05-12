package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;

public class LogoutCommand implements Command {

    @Override
    public Response execute(Request request) {
        request.session().logout();
        return new ResponseImpl("Logged out.");
    }
}
