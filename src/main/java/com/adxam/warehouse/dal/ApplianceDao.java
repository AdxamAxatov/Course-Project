package com.adxam.warehouse.dal;

import com.adxam.warehouse.entity.Appliance;
import com.adxam.warehouse.criteria.SearchCriteria;
import java.util.List;

public interface ApplianceDao<A extends Appliance<?>> {
    List<A> find(SearchCriteria<A> criteria) throws DaoException;
}
