package com.adxam.warehouse.service;

public class UserServiceFactory {
    private static UserService instance;

    public static void init(UserService service) {
        if (instance == null) instance = service;
    }

    public static UserService getInstance() {
        return instance;
    }
}
