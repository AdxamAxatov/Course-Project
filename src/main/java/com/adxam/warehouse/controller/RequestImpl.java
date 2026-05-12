package com.adxam.warehouse.controller;

public record RequestImpl(String requestString, String[] tokens, Session session) implements Request {

    public static RequestImpl parse(String line, Session session) {
        String trimmed = line == null ? "" : line.trim();
        String[] tokens = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
        return new RequestImpl(trimmed, tokens, session);
    }
}
