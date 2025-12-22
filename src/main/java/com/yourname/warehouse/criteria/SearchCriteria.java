package com.yourname.warehouse.criteria;

import com.yourname.warehouse.entity.Appliance;

public interface SearchCriteria<A extends Appliance<?>> {
    <P extends Parameter<A>> SearchCriteria<A> add(P parameter);
    boolean test(A appliance);
}