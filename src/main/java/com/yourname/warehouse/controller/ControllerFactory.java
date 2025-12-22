package com.yourname.warehouse.controller;
public class ControllerFactory {
    private static Controller instance;
    public static void init(Controller controller) { if (instance == null) instance = controller; }
    public static Controller getInstance() { return instance; }
}