package com.adxam.warehouse.service;

import com.adxam.warehouse.criteria.SearchQuery;
import com.adxam.warehouse.entity.*;
import java.util.List;

public interface ApplianceService {
    List<Laptop> findLaptops() throws ServiceException;
    List<Oven> findOvens() throws ServiceException;
    Appliance<?> findCheapest() throws ServiceException;
    List<Appliance<?>> findByPrice(Range<Long> range) throws ServiceException;
    List<Appliance<?>> search(SearchQuery query) throws ServiceException;
    long calculateLaptopsCost() throws ServiceException;
    long calculateOvensCost() throws ServiceException;
    long calculateTotalCost() throws ServiceException;
}
