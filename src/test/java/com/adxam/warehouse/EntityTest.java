package com.adxam.warehouse;

import com.adxam.warehouse.entity.Laptop;
import com.adxam.warehouse.entity.Oven;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityTest {

    @Test
    void laptopStoresCommonAndSpecificFields() {
        Laptop laptop = new Laptop()
                .setName("Dell XPS")
                .setWeight(2.0)
                .setPrice(1800)
                .setQuantity(2)
                .setOs("WIN10")
                .setCpu("Intel i5")
                .setBatteryCapacity(350);

        assertEquals("Dell XPS", laptop.getName());
        assertEquals(2.0, laptop.getWeight());
        assertEquals(1800, laptop.getPrice());
        assertEquals(2, laptop.getQuantity());
        assertEquals("WIN10", laptop.getOs());
        assertEquals("Intel i5", laptop.getCpu());
        assertEquals(350, laptop.getBatteryCapacity());
    }

    @Test
    void ovenStoresCommonAndSpecificFields() {
        Oven oven = new Oven()
                .setName("Bosch Oven")
                .setWeight(18.0)
                .setPrice(650)
                .setQuantity(1)
                .setPowerConsumption(2100)
                .setCapacity(33.0);

        assertEquals("Bosch Oven", oven.getName());
        assertEquals(18.0, oven.getWeight());
        assertEquals(650, oven.getPrice());
        assertEquals(1, oven.getQuantity());
        assertEquals(2100, oven.getPowerConsumption());
        assertEquals(33.0, oven.getCapacity());
    }
}
