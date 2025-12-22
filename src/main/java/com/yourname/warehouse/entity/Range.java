package com.yourname.warehouse.entity;

public record Range<T extends Comparable<T>>(T min, T max) {
    public boolean contains(T value) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
}