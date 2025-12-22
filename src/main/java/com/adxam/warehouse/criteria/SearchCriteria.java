package com.adxam.warehouse.criteria;

import com.adxam.warehouse.entity.Appliance;

public interface SearchCriteria<A extends Appliance<?>> {
    <P extends Parameter<A>> SearchCriteria<A> add(P parameter);
    boolean test(A appliance);
}
