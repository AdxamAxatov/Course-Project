package com.adxam.warehouse.view;

public class ViewFactory {
    private static View instance;

    public static void init(View view) {
        if (instance == null) instance = view;
    }

    public static View getInstance() {
        return instance;
    }
}
