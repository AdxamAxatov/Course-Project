package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.entity.Role;

import java.util.EnumSet;
import java.util.Set;

public interface Command {

    Response execute(Request request);

    default boolean requiresAuth() {
        return true;
    }

    default Set<Role> allowedRoles() {
        return EnumSet.of(Role.ADMIN, Role.USER, Role.VISITOR);
    }
}
