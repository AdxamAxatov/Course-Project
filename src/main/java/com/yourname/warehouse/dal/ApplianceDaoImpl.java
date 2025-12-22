package com.yourname.warehouse.dal;

import com.yourname.warehouse.entity.Appliance;
import com.yourname.warehouse.criteria.SearchCriteria;
import com.yourname.warehouse.source.Source;
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
            src.init(); // <--- ADD THIS LINE to open the file
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
