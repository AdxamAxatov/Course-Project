package com.yourname.warehouse.criteria;

import com.yourname.warehouse.entity.Appliance;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCriteria<A extends Appliance<?>> implements SearchCriteria<A> {
    private final List<Parameter<A>> parameters = new ArrayList<>();

    @Override
    public <P extends Parameter<A>> SearchCriteria<A> add(P parameter) {
        parameters.add(parameter);
        return this;
    }

    @Override
    public boolean test(A appliance) {
        // Must satisfy all parameters
        return parameters.stream().allMatch(p -> p.test(appliance));
    }
}