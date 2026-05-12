package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.entity.User;
import com.adxam.warehouse.service.AuthService;
import com.adxam.warehouse.service.AuthServiceFactory;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.util.CommandArgs;

import java.util.Optional;

public class LoginCommand implements Command {

    @Override
    public boolean requiresAuth() {
        return false;
    }

    @Override
    public Response execute(Request request) {
        CommandArgs args = new CommandArgs(request.tokens(), 1);
        String username = args.requireString("username");
        String password = args.requireString("password");
        AuthService auth = AuthServiceFactory.getInstance();
        try {
            Optional<User> result = auth.authenticate(username, password);
            if (result.isEmpty()) {
                return new ResponseImpl("Invalid credentials.", false, false);
            }
            User user = result.get();
            request.session().login(user);
            return new ResponseImpl("Logged in as " + user.getUsername() + " (" + user.getRole() + ")");
        } catch (ServiceException e) {
            return new ResponseImpl("Login error: " + e.getMessage(), false, false);
        }
    }
}
