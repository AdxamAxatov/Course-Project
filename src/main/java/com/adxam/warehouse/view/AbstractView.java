package com.adxam.warehouse.view;

import com.adxam.warehouse.controller.Controller;
import com.adxam.warehouse.controller.ControllerFactory;

public abstract class AbstractView implements View {

    protected Controller controller() {
        Controller controller = ControllerFactory.getInstance();
        if (controller == null) {
            throw new IllegalStateException("Controller is not initialized. Check configuration.");
        }
        return controller;
    }

    @Override
    public void crash() {
        System.out.println("Sorry, something went wrong...");
    }
}
