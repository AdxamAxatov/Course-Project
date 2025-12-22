package com.adxam.warehouse.dal;

import com.adxam.warehouse.entity.Appliance;
import com.adxam.warehouse.criteria.SearchCriteria;
import com.adxam.warehouse.source.Source;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApplianceDaoImpl<A extends Appliance<?>> implements ApplianceDao<A> {
    private final Source<A> source;

    public ApplianceDaoImpl(Source<A> source) {
        this.source = source;
    }

    @Override
    public List<A> find(SearchCriteria<A> criteria) throws DaoException {
        List<A> list = new ArrayList<>();
        try (Source<A> src = source.copy()) {
            src.init(); 
            while (src.hasNext()) {
                A appliance = src.next();
                if (criteria.test(appliance))
                    list.add(appliance);
            }
        } catch (IOException e) {
            throw new DaoException(e);
        }
        return list;    
    }
}
