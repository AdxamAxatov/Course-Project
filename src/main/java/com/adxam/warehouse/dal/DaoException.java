package com.adxam.warehouse.dal;

public class DaoException extends Exception {
    public DaoException(String message) { super(message); }
    public DaoException(Throwable cause) { super(cause); }
    public DaoException(String message, Throwable cause) { super(message, cause); }
}
