package com.yourname.warehouse.entity;

import com.yourname.warehouse.interfacepkg.PowerConsumable;

public class Oven extends Appliance<Oven> implements PowerConsumable {
    private int powerConsumption;
    private double capacity;

    public Oven setPowerConsumption(int powerConsumption) { this.powerConsumption = powerConsumption; return this; }
    public Oven setCapacity(double capacity) { this.capacity = capacity; return this; }

    @Override
    public int getPowerConsumption() { return powerConsumption; }
    public double getCapacity() { return capacity; }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + 
               String.format(", power=%d, capacity=%.1f}", powerConsumption, capacity);
    }
}