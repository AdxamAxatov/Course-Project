package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.service.UserService;
import com.adxam.warehouse.service.UserServiceFactory;
import com.adxam.warehouse.util.CommandArgs;
import com.adxam.warehouse.util.ValidationException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class UsersCommand implements Command {

    @Override
    public Set<Role> allowedRoles() {
        return EnumSet.of(Role.ADMIN);
    }

    @Override
    public Response execute(Request request) {
        CommandArgs args = new CommandArgs(request.tokens(), 1);
        String sub = args.requireString("subcommand (list|add|delete)").toLowerCase();
        UserService service = UserServiceFactory.getInstance();
        try {
            return switch (sub) {
                case "list" -> list(service);
                case "add" -> add(args, service);
                case "delete", "remove" -> delete(args, service, request);
                default -> throw new ValidationException("Unknown subcommand: " + sub);
            };
        } catch (ServiceException e) {
            return new ResponseImpl("Users command failed: " + e.getMessage(), false, false);
        }
    }

    private Response list(UserService service) throws ServiceException {
        List<User> users = service.listAll();
        if (users.isEmpty()) return new ResponseImpl("No users.");
        StringBuilder sb = new StringBuilder();
        for (User u : users) sb.append(u).append(System.lineSeparator());
        return new ResponseImpl(sb.toString().stripTrailing());
    }

    private Response add(CommandArgs args, UserService service) throws ServiceException {
        String username = args.requireString("username");
        String password = args.requireString("password");
        String roleRaw = args.requireString("role (ADMIN|USER|VISITOR)");
        Role role;
        try {
            role = Role.parse(roleRaw);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
        User created = service.create(username, password, role);
        return new ResponseImpl("User created: " + created);
    }

    private Response delete(CommandArgs args, UserService service, Request request) throws ServiceException {
        long id = args.requireLong("id");
        long selfId = request.session().currentUser().map(User::getId).orElse(-1L);
        if (id == selfId) {
            return new ResponseImpl("Refusing to delete the currently logged-in admin.", false, false);
        }
        boolean removed = service.delete(id);
        return new ResponseImpl(removed
                ? "User deleted: id=" + id
                : "No user found with id=" + id);
    }
}
