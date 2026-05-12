package com.adxam.warehouse.entity;

public enum Role {
    ADMIN,
    USER,
    VISITOR;

    public static Role parse(String value) {
        if (value == null) throw new IllegalArgumentException("Role cannot be null");
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown role: " + value);
        }
    }
}
