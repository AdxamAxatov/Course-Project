package com.adxam.warehouse.service;

public class ServiceException extends Exception {
    public ServiceException(String message) { super(message); }
    public ServiceException(Exception e) { super(e); }
    public ServiceException(String message, Throwable cause) { super(message, cause); }
}
