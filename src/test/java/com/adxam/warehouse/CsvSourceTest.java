package com.adxam.warehouse;

import com.adxam.warehouse.entity.Laptop;
import com.adxam.warehouse.entity.Oven;
import com.adxam.warehouse.source.LaptopCsvSourceImpl;
import com.adxam.warehouse.source.OvenCsvSourceImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvSourceTest {

    @Test
    void laptopSourceParsesRowsFromTestResource() throws IOException {
        try (LaptopCsvSourceImpl source = new LaptopCsvSourceImpl("laptops1-test.csv")) {
            source.init();

            assertTrue(source.hasNext());
            Laptop laptop = source.next();

            assertEquals("TestBook Basic", laptop.getName());
            assertEquals(1.8, laptop.getWeight());
            assertEquals(900, laptop.getPrice());
            assertEquals(4, laptop.getQuantity());
            assertEquals("WIN11", laptop.getOs());
            assertEquals("Intel i3", laptop.getCpu());
            assertEquals(300, laptop.getBatteryCapacity());
        }
    }

    @Test
    void ovenSourceParsesRowsFromTestResource() throws IOException {
        try (OvenCsvSourceImpl source = new OvenCsvSourceImpl("ovens1-test.csv")) {
            source.init();

            assertTrue(source.hasNext());
            Oven oven = source.next();

            assertEquals("Compact Test Oven", oven.getName());
            assertEquals(16.0, oven.getWeight());
            assertEquals(500, oven.getPrice());
            assertEquals(2, oven.getQuantity());
            assertEquals(1800, oven.getPowerConsumption());
            assertEquals(28.0, oven.getCapacity());
        }
    }

    @Test
    void missingSourceFailsDuringInitialization() {
        LaptopCsvSourceImpl source = new LaptopCsvSourceImpl("missing.csv");

        assertThrows(IOException.class, source::init);
    }
}
