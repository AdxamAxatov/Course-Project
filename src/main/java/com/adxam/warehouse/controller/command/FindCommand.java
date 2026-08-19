package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;
import com.adxam.warehouse.criteria.SearchQuery;
import com.adxam.warehouse.entity.Appliance;
import com.adxam.warehouse.service.ApplianceService;
import com.adxam.warehouse.service.ServiceException;
import com.adxam.warehouse.service.ServiceFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class FindCommand implements Command {

    private static final String USAGE =
            "Format: find [laptops|ovens|all] [parameter=value]... [sort=field asc|desc]";

    private static final Set<String> COMMON_PARAMETERS = Set.of("id", "name", "price", "weight", "quantity");
    private static final Set<String> LAPTOP_PARAMETERS = Set.of("os", "cpu", "battery");
    private static final Set<String> OVEN_PARAMETERS = Set.of("power", "capacity");
    private static final Set<String> SORT_FIELDS = Set.of("id", "name", "category", "price", "quantity", "weight");

    private static final String SORT = "sort";
    private static final String LAPTOPS = "laptops";
    private static final String OVENS = "ovens";
    private static final String ALL = "all";

    @Override
    public Response execute(String[] args) {
        if (args.length < 2) {
            return error(USAGE);
        }

        String category = category(args[1]);
        if (category == null) {
            return error("Unknown category: '" + args[1] + "'. " + USAGE);
        }

        Map<String, String> parameters;
        try {
            parameters = parseParameters(args);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }

        String sortField = null;
        boolean descending = false;
        String sortValue = parameters.remove(SORT);

        if (sortValue != null) {
            String[] sortParts = sortValue.split("\\s+");
            if (sortParts.length > 2) {
                return error("Format: sort=field [asc|desc]");
            }

            sortField = sortParts[0].toLowerCase();
            if (!SORT_FIELDS.contains(sortField)) {
                return error("Cannot sort by '" + sortField + "'. Available fields: " + listOf(SORT_FIELDS));
            }

            if (sortParts.length == 2) {
                String order = sortParts[1].toLowerCase();
                if (!order.equals("asc") && !order.equals("desc")) {
                    return error("Sort order must be 'asc' or 'desc', but was: " + sortParts[1]);
                }
                descending = order.equals("desc");
            }
        }

        for (String parameter : parameters.keySet()) {
            String rejection = reject(parameter, category);
            if (rejection != null) {
                return error(rejection);
            }
        }

        try {
            ApplianceService service = ServiceFactory.getInstance();
            List<Appliance<?>> found = service.search(new SearchQuery(category, parameters, sortField, descending));

            return new ResponseImpl(ApplianceFormatter.format(found));
        } catch (ServiceException e) {
            return error(e.getMessage());
        }
    }

    private String category(String value) {
        String category = value.toLowerCase();

        if (category.startsWith("laptop")) {
            return LAPTOPS;
        }
        if (category.startsWith("oven")) {
            return OVENS;
        }
        if (category.equals(ALL)) {
            return ALL;
        }
        return null;
    }

    /**
     * Reads {@code key=value} pairs. A token without {@code =} continues the previous value,
     * so a value may contain spaces, as in {@code cpu=Intel i5}.
     */
    private Map<String, String> parseParameters(String[] args) {
        Map<String, String> parameters = new LinkedHashMap<>();
        String currentKey = null;

        for (int i = 2; i < args.length; i++) {
            String token = args[i];
            int separator = token.indexOf('=');

            if (separator > 0) {
                currentKey = token.substring(0, separator).toLowerCase();
                if (parameters.containsKey(currentKey)) {
                    throw new IllegalArgumentException("Parameter '" + currentKey + "' was given more than once.");
                }
                parameters.put(currentKey, token.substring(separator + 1).trim());
            } else if (currentKey != null) {
                parameters.put(currentKey, (parameters.get(currentKey) + " " + token).trim());
            } else {
                throw new IllegalArgumentException("Expected parameter=value, but was: '" + token + "'. " + USAGE);
            }
        }

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            if (parameter.getValue().isEmpty()) {
                throw new IllegalArgumentException("Parameter '" + parameter.getKey() + "' has no value.");
            }
        }

        return parameters;
    }

    private String reject(String parameter, String category) {
        if (COMMON_PARAMETERS.contains(parameter)) {
            return null;
        }
        if (LAPTOP_PARAMETERS.contains(parameter)) {
            return category.equals(OVENS)
                    ? "Parameter '" + parameter + "' is only available for laptops."
                    : null;
        }
        if (OVEN_PARAMETERS.contains(parameter)) {
            return category.equals(LAPTOPS)
                    ? "Parameter '" + parameter + "' is only available for ovens."
                    : null;
        }
        return "Unknown parameter: '" + parameter + "'. Available: " + available(category);
    }

    private String available(String category) {
        Set<String> parameters = new TreeSet<>(COMMON_PARAMETERS);
        if (!category.equals(OVENS)) {
            parameters.addAll(LAPTOP_PARAMETERS);
        }
        if (!category.equals(LAPTOPS)) {
            parameters.addAll(OVEN_PARAMETERS);
        }
        parameters.add(SORT);
        return String.join(", ", parameters);
    }

    private String listOf(Set<String> values) {
        return String.join(", ", new TreeSet<>(values));
    }

    private Response error(String message) {
        return new ResponseImpl(message, false, false);
    }
}
