package com.adxam.warehouse.service;

import com.adxam.warehouse.dal.*;
import com.adxam.warehouse.entity.*;
import com.adxam.warehouse.criteria.*;
import java.util.*;

public class ApplianceServiceImpl implements ApplianceService {

    @Override
    public List<Laptop> findLaptops() throws ServiceException {
        try {
            return DaoFactory.getInstance(Laptop.class)
                    .find(new LaptopSearchCriteria().add(Parameter.any()));
        } catch (DaoException e) {
            throw new ServiceException("Could not fetch laptops: " + e.getMessage());
        }
    }

    @Override
    public List<Oven> findOvens() throws ServiceException {
        try {
            return DaoFactory.getInstance(Oven.class)
                    .find(new OvenSearchCriteria().add(Parameter.any()));
        } catch (DaoException e) {
            throw new ServiceException("Could not fetch ovens: " + e.getMessage());
        }
    }

    @Override
    public Appliance<?> findCheapest() throws ServiceException {
        try {
            List<Appliance<?>> allItems = new ArrayList<>();
            allItems.addAll(findLaptops());
            allItems.addAll(findOvens());

            return allItems.stream()
                    .min(Comparator.comparingLong(Appliance::getPrice))
                    .orElse(null);
        } catch (Exception e) {
            throw new ServiceException("Error calculating cheapest appliance.");
        }
    }

    @Override
    public List<Appliance<?>> findByPrice(Range<Long> range) throws ServiceException {
        try {
            List<Appliance<?>> all = new ArrayList<>();
            
            all.addAll(DaoFactory.getInstance(Oven.class)
                    .find(new OvenSearchCriteria().add(o -> range.contains(o.getPrice()))));
            
            all.addAll(DaoFactory.getInstance(Laptop.class)
                    .find(new LaptopSearchCriteria().add(l -> range.contains(l.getPrice()))));
            
            return all;
        } catch (DaoException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
