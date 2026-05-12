package com.adxam.warehouse.controller;

import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void unauthenticatedSessionHasVisitorRole() {
        Session s = new Session();
        assertFalse(s.isAuthenticated());
        assertEquals(Role.VISITOR, s.currentRole());
        assertTrue(s.currentUser().isEmpty());
    }

    @Test
    void loginSetsCurrentUser() {
        Session s = new Session();
        User u = new User().setId(1).setUsername("alice").setRole(Role.ADMIN);
        s.login(u);
        assertTrue(s.isAuthenticated());
        assertEquals(Role.ADMIN, s.currentRole());
        assertEquals(u, s.currentUser().orElseThrow());
    }

    @Test
    void enterAsVisitorAuthenticatesWithVisitorRole() {
        Session s = new Session();
        s.enterAsVisitor();
        assertTrue(s.isAuthenticated());
        assertEquals(Role.VISITOR, s.currentRole());
    }

    @Test
    void logoutClearsUser() {
        Session s = new Session();
        s.login(new User().setUsername("bob").setRole(Role.USER));
        s.logout();
        assertFalse(s.isAuthenticated());
        assertEquals(Role.VISITOR, s.currentRole());
    }
}
