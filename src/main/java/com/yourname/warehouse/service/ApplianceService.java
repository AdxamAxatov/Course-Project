package com.yourname.warehouse.service;

import com.yourname.warehouse.entity.*;
import java.util.List;

public interface ApplianceService {
    List<Laptop> findLaptops() throws ServiceException;
    List<Oven> findOvens() throws ServiceException;
    Appliance<?> findCheapest() throws ServiceException;
    List<Appliance<?>> findByPrice(Range<Long> range) throws ServiceException;
}