package com.adxam.warehouse.controller;

public interface Request {
    String requestString();
    String[] tokens();
    Session session();
}
