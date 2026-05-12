package com.adxam.warehouse.service;

import com.adxam.warehouse.entity.*;
import java.util.List;

public interface ApplianceService {
    List<Laptop> findLaptops() throws ServiceException;
    List<Oven> findOvens() throws ServiceException;
    Appliance<?> findCheapest() throws ServiceException;
    List<Appliance<?>> findByPrice(Range<Long> range) throws ServiceException;

    Laptop addLaptop(Laptop laptop) throws ServiceException;
    Oven addOven(Oven oven) throws ServiceException;
    boolean removeLaptop(long id) throws ServiceException;
    boolean removeOven(long id) throws ServiceException;
}
