package com.adxam.warehouse.entity;

public abstract class Appliance<SELF extends Appliance<SELF>> {
    private String id;
    private String name;
    private double weight;
    private long price;
    private int quantity;

    @SuppressWarnings("unchecked")
    public SELF setId(String id) { this.id = id; return (SELF) this; }
    @SuppressWarnings("unchecked")
    public SELF setName(String name) { this.name = name; return (SELF) this; }
    @SuppressWarnings("unchecked")
    public SELF setWeight(double weight) { this.weight = weight; return (SELF) this; }
    @SuppressWarnings("unchecked")
    public SELF setPrice(long price) { this.price = price; return (SELF) this; }
    @SuppressWarnings("unchecked")
    public SELF setQuantity(int quantity) { this.quantity = quantity; return (SELF) this; }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getWeight() { return weight; }
    public long getPrice() { return price; }
    public int getQuantity() { return quantity; }

    /** Product category shown to the user, e.g. {@code Laptop}. */
    public String category() {
        return getClass().getSimpleName();
    }

    /** The fields that only this kind of appliance has, already formatted for display. */
    public abstract String details();

    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', weight=%.1f, price=%d, quantity=%d, %s}",
            category(), id, name, weight, price, quantity, details());
    }
}
