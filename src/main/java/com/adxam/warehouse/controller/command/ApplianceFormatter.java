package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.entity.Appliance;

import java.util.List;

/** Turns a search result into the text the user sees. */
final class ApplianceFormatter {

    static final String NO_RESULTS = "No matching products were found.";

    private static final String ROW = "%-6s %-18s %-9s %8s %5s %8s  %s";

    private ApplianceFormatter() {
    }

    static String format(List<? extends Appliance<?>> appliances) {
        if (appliances == null || appliances.isEmpty()) {
            return NO_RESULTS;
        }

        String header = String.format(ROW, "ID", "NAME", "CATEGORY", "PRICE", "QTY", "WEIGHT", "DETAILS");
        String separator = "-".repeat(header.length());

        StringBuilder table = new StringBuilder();
        table.append(header).append(System.lineSeparator());
        table.append(separator).append(System.lineSeparator());

        for (Appliance<?> appliance : appliances) {
            table.append(String.format(ROW,
                            appliance.getId(),
                            appliance.getName(),
                            appliance.category(),
                            appliance.getPrice(),
                            appliance.getQuantity(),
                            String.format("%.1f", appliance.getWeight()),
                            appliance.details()))
                    .append(System.lineSeparator());
        }

        table.append(separator).append(System.lineSeparator());
        table.append(appliances.size()).append(" product(s) found.");

        return table.toString();
    }
}
