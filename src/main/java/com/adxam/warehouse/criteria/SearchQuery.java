package com.adxam.warehouse.criteria;

import java.util.Map;

/**
 * A user search request after the controller has parsed it: which category to look in,
 * which field filters to apply, and how to order the result.
 */
public record SearchQuery(String category, Map<String, String> filters, String sortField, boolean descending) {

    public SearchQuery {
        filters = Map.copyOf(filters);
    }

    public boolean isSorted() {
        return sortField != null;
    }
}
