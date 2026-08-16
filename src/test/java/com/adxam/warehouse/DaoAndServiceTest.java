package com.adxam.warehouse;

import com.adxam.warehouse.criteria.LaptopSearchCriteria;
import com.adxam.warehouse.criteria.Parameter;
import com.adxam.warehouse.dal.ApplianceDao;
import com.adxam.warehouse.dal.ApplianceDaoImpl;
import com.adxam.warehouse.dal.DaoException;
import com.adxam.warehouse.dal.DaoFactory;
import com.adxam.warehouse.entity.Appliance;
import com.adxam.warehouse.entity.Laptop;
import com.adxam.warehouse.entity.Oven;
import com.adxam.warehouse.entity.Range;
import com.adxam.warehouse.service.ApplianceService;
import com.adxam.warehouse.service.ApplianceServiceImpl;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.service.ServiceFactory;
import com.adxam.warehouse.source.LaptopCsvSourceImpl;
import com.adxam.warehouse.source.OvenCsvSourceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DaoAndServiceTest {

    @BeforeAll
    static void configureFactories() {
        DaoFactory.init(Map.of(
                Laptop.class, new ApplianceDaoImpl<>(new LaptopCsvSourceImpl("laptops1-test.csv")),
                Oven.class, new ApplianceDaoImpl<>(new OvenCsvSourceImpl("ovens1-test.csv"))));
        ServiceFactory.init(new ApplianceServiceImpl());
    }

    @Test
    void daoFindsLaptopByCriteria() throws DaoException {
        ApplianceDao<Laptop> dao = DaoFactory.getInstance(Laptop.class);

        List<Laptop> result = dao.find(new LaptopSearchCriteria()
                .add(laptop -> "Intel i5".equals(laptop.getCpu())));

        assertEquals(1, result.size());
        assertEquals("TestBook Pro", result.get(0).getName());
    }

    @Test
    void daoFindsAllLaptops() throws DaoException {
        ApplianceDao<Laptop> dao = DaoFactory.getInstance(Laptop.class);

        List<Laptop> result = dao.find(new LaptopSearchCriteria().add(Parameter.any()));

        assertEquals(3, result.size());
    }

    @Test
    void serviceFindsAppliancesByPriceRange() throws ServiceException {
        ApplianceService service = ServiceFactory.getInstance();

        List<Appliance<?>> result = service.findByPrice(new Range<>(900L, 1100L));

        assertEquals(3, result.size());
    }

    @Test
    void serviceFindsCheapestAppliance() throws ServiceException {
        ApplianceService service = ServiceFactory.getInstance();

        Appliance<?> cheapest = service.findCheapest();

        assertInstanceOf(Oven.class, cheapest);
        assertEquals("Compact Test Oven", cheapest.getName());
    }
}
