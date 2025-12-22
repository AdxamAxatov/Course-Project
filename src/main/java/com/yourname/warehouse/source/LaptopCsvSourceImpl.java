package com.yourname.warehouse.source;

import com.yourname.warehouse.entity.Laptop;

public class LaptopCsvSourceImpl extends AbstractCsvSource<Laptop> {
    public LaptopCsvSourceImpl(String csvName) { super(csvName); }

    @Override
    public Laptop next() {
        String[] data = getLine().split(";");
        return new Laptop()
                .setName(data[0].trim())
                .setWeight(Double.parseDouble(data[1].trim()))
                .setPrice(Long.parseLong(data[2].trim()))
                .setQuantity(Integer.parseInt(data[3].trim()))
                .setOs(data[4].trim())
                .setCpu(data[5].trim())
                .setBatteryCapacity(Integer.parseInt(data[6].trim()));
    }

    @Override
    public Source<Laptop> copy() { return new LaptopCsvSourceImpl(csvName()); }
}