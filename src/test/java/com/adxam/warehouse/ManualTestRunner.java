package com.adxam.warehouse;

import com.adxam.warehouse.criteria.LaptopSearchCriteria;
import com.adxam.warehouse.criteria.OvenSearchCriteria;
import com.adxam.warehouse.criteria.Parameter;
import com.adxam.warehouse.dal.ApplianceDao;
import com.adxam.warehouse.dal.ApplianceDaoImpl;
import com.adxam.warehouse.dal.DaoException;
import com.adxam.warehouse.dal.DaoFactory;
import com.adxam.warehouse.entity.Laptop;
import com.adxam.warehouse.entity.Oven;
import com.adxam.warehouse.service.ApplianceServiceImpl;
import com.adxam.warehouse.service.ServiceFactory;
import com.adxam.warehouse.source.LaptopCsvSourceImpl;
import com.adxam.warehouse.source.OvenCsvSourceImpl;

import java.util.List;
import java.util.Map;

public class ManualTestRunner {

    public static void main(String[] args) {
        try {
            initFactoriesForTestData();

            shouldFindAllLaptops();
            shouldFindAllOvens();
            shouldFindLaptopsByCpu();

            System.out.println("Manual tests: OK");
        } catch (AssertionError ae) {
            System.out.println("Manual tests: FAILED");
            System.out.println(ae.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Manual tests: ERROR");
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void initFactoriesForTestData() {
        DaoFactory.init(Map.of(
                Laptop.class, new ApplianceDaoImpl<>(new LaptopCsvSourceImpl("laptops.csv")),
                Oven.class, new ApplianceDaoImpl<>(new OvenCsvSourceImpl("ovens.csv"))));

        ServiceFactory.init(new ApplianceServiceImpl());
    }

    private static void shouldFindAllLaptops() throws DaoException {
        ApplianceDao<Laptop> dao = DaoFactory.getInstance(Laptop.class);
        List<Laptop> laptops = dao.find(new LaptopSearchCriteria().add(Parameter.any()));
        assert laptops.size() == 3 : "Expected 3 laptops, got " + laptops.size();
    }

    private static void shouldFindAllOvens() throws DaoException {
        ApplianceDao<Oven> dao = DaoFactory.getInstance(Oven.class);
        List<Oven> ovens = dao.find(new OvenSearchCriteria().add(Parameter.any()));
        assert ovens.size() == 3 : "Expected 3 ovens, got " + ovens.size();
    }

    private static void shouldFindLaptopsByCpu() throws DaoException {
        ApplianceDao<Laptop> dao = DaoFactory.getInstance(Laptop.class);
        List<Laptop> result = dao.find(
                new LaptopSearchCriteria().add(l -> "Intel i5".equals(l.getCpu())));
        assert result.size() == 1 : "Expected 1 laptop with Intel i5, got " + result.size();
    }
}
