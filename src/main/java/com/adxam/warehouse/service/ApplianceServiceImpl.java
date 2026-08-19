package com.adxam.warehouse.service;

import com.adxam.warehouse.dal.*;
import com.adxam.warehouse.entity.*;
import com.adxam.warehouse.criteria.*;
import java.util.*;

public class ApplianceServiceImpl implements ApplianceService {

    private static final Set<String> LAPTOP_ONLY = Set.of("os", "cpu", "battery");
    private static final Set<String> OVEN_ONLY = Set.of("power", "capacity");

    private static final String LAPTOPS = "laptops";
    private static final String OVENS = "ovens";
    private static final String ALL = "all";

    @Override
    public List<Laptop> findLaptops() throws ServiceException {
        try {
            return DaoFactory.getInstance(Laptop.class)
                    .find(new LaptopSearchCriteria().add(Parameter.any()));
        } catch (DaoException e) {
            throw new ServiceException("Could not fetch laptops", e);
        }
    }

    @Override
    public List<Oven> findOvens() throws ServiceException {
        try {
            return DaoFactory.getInstance(Oven.class)
                    .find(new OvenSearchCriteria().add(Parameter.any()));
        } catch (DaoException e) {
            throw new ServiceException("Could not fetch ovens", e);
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
            throw new ServiceException("Could not fetch appliances by price", e);
        }
    }

    @Override
    public List<Appliance<?>> search(SearchQuery query) throws ServiceException {
        List<Appliance<?>> found = new ArrayList<>();

        if (includesLaptops(query)) {
            found.addAll(searchLaptops(query.filters()));
        }
        if (includesOvens(query)) {
            found.addAll(searchOvens(query.filters()));
        }

        sort(found, query);
        return found;
    }

    private boolean includesLaptops(SearchQuery query) {
        if (LAPTOPS.equals(query.category())) {
            return true;
        }
        return ALL.equals(query.category()) && !containsAny(query.filters(), OVEN_ONLY);
    }

    private boolean includesOvens(SearchQuery query) {
        if (OVENS.equals(query.category())) {
            return true;
        }
        return ALL.equals(query.category()) && !containsAny(query.filters(), LAPTOP_ONLY);
    }

    private boolean containsAny(Map<String, String> filters, Set<String> keys) {
        return filters.keySet().stream().anyMatch(keys::contains);
    }

    private List<Laptop> searchLaptops(Map<String, String> filters) throws ServiceException {
        SearchCriteria<Laptop> criteria = new LaptopSearchCriteria();

        for (Map.Entry<String, String> filter : filters.entrySet()) {
            Parameter<Laptop> parameter = commonParameter(filter.getKey(), filter.getValue());
            if (parameter == null) {
                parameter = laptopParameter(filter.getKey(), filter.getValue());
            }
            if (parameter != null) {
                criteria.add(parameter);
            }
        }

        try {
            return DaoFactory.getInstance(Laptop.class).find(criteria);
        } catch (DaoException e) {
            throw new ServiceException("Could not fetch laptops", e);
        }
    }

    private List<Oven> searchOvens(Map<String, String> filters) throws ServiceException {
        SearchCriteria<Oven> criteria = new OvenSearchCriteria();

        for (Map.Entry<String, String> filter : filters.entrySet()) {
            Parameter<Oven> parameter = commonParameter(filter.getKey(), filter.getValue());
            if (parameter == null) {
                parameter = ovenParameter(filter.getKey(), filter.getValue());
            }
            if (parameter != null) {
                criteria.add(parameter);
            }
        }

        try {
            return DaoFactory.getInstance(Oven.class).find(criteria);
        } catch (DaoException e) {
            throw new ServiceException("Could not fetch ovens", e);
        }
    }

    private <A extends Appliance<?>> Parameter<A> commonParameter(String key, String value) throws ServiceException {
        switch (key) {
            case "id":
                return appliance -> containsIgnoreCase(appliance.getId(), value);
            case "name":
                return appliance -> containsIgnoreCase(appliance.getName(), value);
            case "price": {
                Range<Double> range = numericRange(key, value);
                return appliance -> range.contains((double) appliance.getPrice());
            }
            case "weight": {
                Range<Double> range = numericRange(key, value);
                return appliance -> range.contains(appliance.getWeight());
            }
            case "quantity": {
                Range<Double> range = numericRange(key, value);
                return appliance -> range.contains((double) appliance.getQuantity());
            }
            default:
                return null;
        }
    }

    private Parameter<Laptop> laptopParameter(String key, String value) throws ServiceException {
        switch (key) {
            case "os":
                return laptop -> containsIgnoreCase(laptop.getOs(), value);
            case "cpu":
                return laptop -> containsIgnoreCase(laptop.getCpu(), value);
            case "battery": {
                Range<Double> range = numericRange(key, value);
                return laptop -> range.contains((double) laptop.getBatteryCapacity());
            }
            default:
                return null;
        }
    }

    private Parameter<Oven> ovenParameter(String key, String value) throws ServiceException {
        switch (key) {
            case "power": {
                Range<Double> range = numericRange(key, value);
                return oven -> range.contains((double) oven.getPowerConsumption());
            }
            case "capacity": {
                Range<Double> range = numericRange(key, value);
                return oven -> range.contains(oven.getCapacity());
            }
            default:
                return null;
        }
    }

    private boolean containsIgnoreCase(String actual, String expected) {
        return actual != null && actual.toLowerCase().contains(expected.toLowerCase());
    }

    private Range<Double> numericRange(String key, String value) throws ServiceException {
        try {
            if (value.contains(";")) {
                String[] bounds = value.split(";");
                if (bounds.length != 2) {
                    throw new NumberFormatException(value);
                }
                return new Range<>(Double.parseDouble(bounds[0].trim()), Double.parseDouble(bounds[1].trim()));
            }
            double exact = Double.parseDouble(value.trim());
            return new Range<>(exact, exact);
        } catch (NumberFormatException e) {
            throw new ServiceException(
                    "Parameter '" + key + "' expects a number or a min;max range, but was: " + value, e);
        }
    }

    private void sort(List<Appliance<?>> appliances, SearchQuery query) throws ServiceException {
        if (!query.isSorted()) {
            return;
        }

        Comparator<Appliance<?>> comparator = switch (query.sortField()) {
            case "id" -> Comparator.comparing(Appliance::getId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "name" -> Comparator.comparing(Appliance::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "category" -> Comparator.comparing(Appliance::category);
            case "price" -> Comparator.comparingLong(Appliance::getPrice);
            case "quantity" -> Comparator.comparingInt(Appliance::getQuantity);
            case "weight" -> Comparator.comparingDouble(Appliance::getWeight);
            default -> throw new ServiceException("Cannot sort by: " + query.sortField());
        };

        appliances.sort(query.descending() ? comparator.reversed() : comparator);
    }

    @Override
    public long calculateLaptopsCost() throws ServiceException {
        return sumCost(findLaptops());
    }

    @Override
    public long calculateOvensCost() throws ServiceException {
        return sumCost(findOvens());
    }

    @Override
    public long calculateTotalCost() throws ServiceException {
        return calculateLaptopsCost() + calculateOvensCost();
    }

    private long sumCost(List<? extends Appliance<?>> appliances) {
        return appliances.stream()
                .mapToLong(appliance -> appliance.getPrice() * appliance.getQuantity())
                .sum();
    }
}
