package com.adxam.warehouse.dal;

import com.adxam.warehouse.entity.Appliance;

public interface MutableApplianceDao<A extends Appliance<?>> extends ApplianceDao<A> {
    A insert(A entity) throws DaoException;
    boolean deleteById(long id) throws DaoException;
}
