package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.entity.User;

public class WhoamiCommand implements Command {

    @Override
    public Response execute(Request request) {
        User user = request.session().currentUser().orElse(null);
        if (user == null) return new ResponseImpl("Not authenticated.");
        return new ResponseImpl(user.getUsername() + " (" + user.getRole() + ")");
    }
}
