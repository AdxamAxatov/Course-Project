package com.yourname.warehouse.service;

public class ServiceFactory {
    private static ApplianceService instance;

    public static void init(ApplianceService service) {
        if (instance == null) instance = service;
    }

    public static ApplianceService getInstance() {
        return instance;
    }
}