package com.adxam.warehouse.util;

import java.util.Arrays;

public class CommandArgs {
    private final String[] tokens;
    private int cursor;

    public CommandArgs(String[] tokens, int start) {
        this.tokens = tokens;
        this.cursor = start;
    }

    public boolean hasMore() {
        return cursor < tokens.length;
    }

    public String peek() {
        if (!hasMore()) return null;
        return tokens[cursor];
    }

    public String requireString(String label) {
        if (!hasMore()) throw new ValidationException("Missing " + label);
        return tokens[cursor++];
    }

    public String optionalString(String label, String def) {
        return hasMore() ? tokens[cursor++] : def;
    }

    public long requireLong(String label) {
        String raw = requireString(label);
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " must be an integer, got: " + raw);
        }
    }

    public int requireInt(String label) {
        long v = requireLong(label);
        if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
            throw new ValidationException(label + " out of int range: " + v);
        }
        return (int) v;
    }

    public double requireDouble(String label) {
        String raw = requireString(label);
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " must be a number, got: " + raw);
        }
    }

    public String joinRest() {
        if (!hasMore()) return "";
        String joined = String.join(" ", Arrays.copyOfRange(tokens, cursor, tokens.length));
        cursor = tokens.length;
        return joined;
    }
}
