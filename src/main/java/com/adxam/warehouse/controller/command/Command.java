package com.adxam.warehouse.controller.command;
import com.adxam.warehouse.controller.Response;
public interface Command { Response execute(String[] args); }
