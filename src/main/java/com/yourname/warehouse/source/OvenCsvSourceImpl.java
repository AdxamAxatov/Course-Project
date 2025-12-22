package com.yourname.warehouse.source;

import com.yourname.warehouse.entity.Oven;

public class OvenCsvSourceImpl extends AbstractCsvSource<Oven> {
    public OvenCsvSourceImpl(String csvName) { super(csvName); }

    @Override
    public Oven next() {
        String[] data = getLine().split(";");
        return new Oven()
                .setName(data[0].trim())
                .setWeight(Double.parseDouble(data[1].trim()))
                .setPrice(Long.parseLong(data[2].trim()))
                .setQuantity(Integer.parseInt(data[3].trim()))
                .setPowerConsumption(Integer.parseInt(data[4].trim()))
                .setCapacity(Double.parseDouble(data[5].trim()));
    }

    @Override
    public Source<Oven> copy() { return new OvenCsvSourceImpl(csvName()); }
}