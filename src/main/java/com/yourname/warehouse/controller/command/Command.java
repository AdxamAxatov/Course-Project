package com.yourname.warehouse.controller.command;
import com.yourname.warehouse.controller.Response;
public interface Command { Response execute(String[] args); }