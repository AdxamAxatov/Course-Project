package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;

public class VisitCommand implements Command {

    @Override
    public boolean requiresAuth() {
        return false;
    }

    @Override
    public Response execute(Request request) {
        request.session().enterAsVisitor();
        return new ResponseImpl("Entered as visitor (read-only access).");
    }
}
