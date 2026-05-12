package com.adxam.warehouse.service;

public class AuthServiceFactory {
    private static AuthService instance;

    public static void init(AuthService service) {
        if (instance == null) instance = service;
    }

    public static AuthService getInstance() {
        return instance;
    }
}
