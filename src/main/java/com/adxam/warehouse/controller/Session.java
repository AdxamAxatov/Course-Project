package com.adxam.warehouse.controller;

import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;

import java.util.Optional;

public class Session {
    private User currentUser;

    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public Role currentRole() {
        return currentUser == null ? Role.VISITOR : currentUser.getRole();
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void enterAsVisitor() {
        this.currentUser = new User().setUsername("visitor").setRole(Role.VISITOR);
    }

    public void logout() {
        this.currentUser = null;
    }
}
