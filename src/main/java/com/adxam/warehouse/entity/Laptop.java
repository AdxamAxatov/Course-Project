package com.adxam.warehouse.entity;

public class Laptop extends Appliance<Laptop> {
    private String os;
    private String cpu;
    private int batteryCapacity;

    public Laptop setOs(String os) { this.os = os; return this; }
    public Laptop setCpu(String cpu) { this.cpu = cpu; return this; }
    public Laptop setBatteryCapacity(int batteryCapacity) { 
        this.batteryCapacity = batteryCapacity; 
        return this; 
    }

    public String getOs() {
        return os;
    }

    public String getCpu() {
        return cpu;
    }

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") +
               String.format(", os='%s', cpu='%s', battery=%d}", os, cpu, batteryCapacity);
    }
}
