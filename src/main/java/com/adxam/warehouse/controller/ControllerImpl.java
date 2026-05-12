package com.adxam.warehouse.controller;

import com.adxam.warehouse.controller.command.Command;
import com.adxam.warehouse.controller.command.CommandProvider;
import com.adxam.warehouse.util.ValidationException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerImpl implements Controller {
    private static final Logger LOG = Logger.getLogger(ControllerImpl.class.getName());

    @Override
    public Response execute(Request request) {
        String[] tokens = request.tokens();
        if (tokens.length == 0) {
            return new ResponseImpl("Type 'help' to see available commands.", true, false);
        }

        Command command = CommandProvider.getCommand(tokens[0]);
        Session session = request.session();

        if (command.requiresAuth() && !session.isAuthenticated()) {
            return new ResponseImpl("Login required. Use: login <username> <password>  (or: visit)", false, false);
        }
        if (command.requiresAuth() && !command.allowedRoles().contains(session.currentRole())) {
            return new ResponseImpl("Permission denied for role: " + session.currentRole(), false, false);
        }

        try {
            return command.execute(request);
        } catch (ValidationException e) {
            return new ResponseImpl("Invalid input: " + e.getMessage(), false, false);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, e, () -> "Unhandled command error: " + tokens[0]);
            return new ResponseImpl("Unexpected error: " + e.getMessage(), false, false);
        }
    }
}
