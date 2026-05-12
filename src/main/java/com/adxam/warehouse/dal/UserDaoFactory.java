package com.adxam.warehouse.dal;

public class UserDaoFactory {
    private static UserDao instance;

    public static void init(UserDao dao) {
        if (instance == null) instance = dao;
    }

    public static UserDao getInstance() {
        return instance;
    }
}
