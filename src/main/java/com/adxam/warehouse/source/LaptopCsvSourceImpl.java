package com.adxam.warehouse.source;

import com.adxam.warehouse.entity.Laptop;

public class LaptopCsvSourceImpl extends AbstractCsvSource<Laptop> {
    public LaptopCsvSourceImpl(String csvName) { super(csvName); }

    @Override
    public Laptop next() {
        String[] data = getLine().split(";");
        return new Laptop()
                .setId(data[0].trim())
                .setName(data[1].trim())
                .setWeight(Double.parseDouble(data[2].trim()))
                .setPrice(Long.parseLong(data[3].trim()))
                .setQuantity(Integer.parseInt(data[4].trim()))
                .setOs(data[5].trim())
                .setCpu(data[6].trim())
                .setBatteryCapacity(Integer.parseInt(data[7].trim()));
    }

    @Override
    public Source<Laptop> copy() { return new LaptopCsvSourceImpl(csvName()); }
}
