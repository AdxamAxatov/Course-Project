package com.yourname.warehouse.criteria;

import com.yourname.warehouse.entity.Appliance;

public interface Parameter<A extends Appliance<?>> {
    boolean test(A appliance);

    static <A extends Appliance<?>> Parameter<A> any() { return appliance -> true; }
    static <A extends Appliance<?>> Parameter<A> none() { return appliance -> false; }
}
