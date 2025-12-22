package com.yourname.warehouse.dal;

import com.yourname.warehouse.entity.Appliance;
import com.yourname.warehouse.criteria.SearchCriteria;
import java.util.List;

public interface ApplianceDao<A extends Appliance<?>> {
    List<A> find(SearchCriteria<A> criteria) throws DaoException;
}
