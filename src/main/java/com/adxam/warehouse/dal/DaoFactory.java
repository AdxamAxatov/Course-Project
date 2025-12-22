package com.adxam.warehouse.dal;

import com.adxam.warehouse.entity.Appliance;
import java.util.Map;

public class DaoFactory {
    private static Map<Class<? extends Appliance<?>>, ApplianceDao<?>> map;

    public static void init(Map<Class<? extends Appliance<?>>, ApplianceDao<?>> daoMap) {
        if (map == null) map = daoMap;
    }

    @SuppressWarnings("unchecked")
    public static <A extends Appliance<?>> ApplianceDao<A> getInstance(Class<A> type) {
        return (ApplianceDao<A>) map.get(type);
    }
}
