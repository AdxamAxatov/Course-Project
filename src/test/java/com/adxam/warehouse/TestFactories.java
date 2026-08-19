package com.adxam.warehouse;

import com.adxam.warehouse.controller.ControllerFactory;
import com.adxam.warehouse.controller.ControllerImpl;
import com.adxam.warehouse.dal.ApplianceDaoImpl;
import com.adxam.warehouse.dal.DaoFactory;
import com.adxam.warehouse.entity.Laptop;
import com.adxam.warehouse.entity.Oven;
import com.adxam.warehouse.service.ApplianceServiceImpl;
import com.adxam.warehouse.service.ServiceFactory;
import com.adxam.warehouse.source.LaptopCsvSourceImpl;
import com.adxam.warehouse.source.OvenCsvSourceImpl;

import java.util.Map;

/**
 * Wires every factory to the test fixtures so that no test ever touches the production CSV files.
 * The factories are write-once, so calling this from several test classes is safe.
 */
final class TestFactories {

    private TestFactories() {
    }

    static void initWithTestResources() {
        DaoFactory.init(Map.of(
                Laptop.class, new ApplianceDaoImpl<>(new LaptopCsvSourceImpl("laptops1-test.csv")),
                Oven.class, new ApplianceDaoImpl<>(new OvenCsvSourceImpl("ovens1-test.csv"))));
        ServiceFactory.init(new ApplianceServiceImpl());
        ControllerFactory.init(new ControllerImpl());
    }
}
