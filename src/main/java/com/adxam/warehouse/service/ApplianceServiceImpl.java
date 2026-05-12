package com.adxam.warehouse.service;

import com.adxam.warehouse.criteria.*;
import com.adxam.warehouse.dal.*;
import com.adxam.warehouse.entity.*;

import java.util.*;
import java.util.logging.Logger;

public class ApplianceServiceImpl implements ApplianceService {
    private static final Logger LOG = Logger.getLogger(ApplianceServiceImpl.class.getName());

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
        List<Appliance<?>> allItems = new ArrayList<>();
        allItems.addAll(findLaptops());
        allItems.addAll(findOvens());
        return allItems.stream()
                .min(Comparator.comparingLong(Appliance::getPrice))
                .orElse(null);
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

    @Override
    public Laptop addLaptop(Laptop laptop) throws ServiceException {
        try {
            Laptop saved = requireMutable(Laptop.class).insert(laptop);
            LOG.info(() -> "Laptop added: " + saved);
            return saved;
        } catch (DaoException e) {
            throw new ServiceException("Failed to add laptop: " + e.getMessage());
        }
    }

    @Override
    public Oven addOven(Oven oven) throws ServiceException {
        try {
            Oven saved = requireMutable(Oven.class).insert(oven);
            LOG.info(() -> "Oven added: " + saved);
            return saved;
        } catch (DaoException e) {
            throw new ServiceException("Failed to add oven: " + e.getMessage());
        }
    }

    @Override
    public boolean removeLaptop(long id) throws ServiceException {
        try {
            boolean removed = requireMutable(Laptop.class).deleteById(id);
            if (removed) LOG.info(() -> "Laptop removed: id=" + id);
            return removed;
        } catch (DaoException e) {
            throw new ServiceException("Failed to remove laptop: " + e.getMessage());
        }
    }

    @Override
    public boolean removeOven(long id) throws ServiceException {
        try {
            boolean removed = requireMutable(Oven.class).deleteById(id);
            if (removed) LOG.info(() -> "Oven removed: id=" + id);
            return removed;
        } catch (DaoException e) {
            throw new ServiceException("Failed to remove oven: " + e.getMessage());
        }
    }

    private <A extends Appliance<?>> MutableApplianceDao<A> requireMutable(Class<A> type) throws ServiceException {
        ApplianceDao<A> dao = DaoFactory.getInstance(type);
        if (!(dao instanceof MutableApplianceDao<A> mutable)) {
            throw new ServiceException("Storage is read-only; cannot modify " + type.getSimpleName());
        }
        return mutable;
    }
}
