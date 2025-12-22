package com.adxam.warehouse.controller;
public record ResponseImpl(String responseString, boolean isOk, boolean isOut) implements Response {
    public ResponseImpl(String responseString) { this(responseString, true, false); }
}
