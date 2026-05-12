package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Request;
import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;

public class HelpCommand implements Command {

    private static final String HELP = String.join(System.lineSeparator(),
            "Available commands:",
            "  login <username> <password>          authenticate",
            "  visit                                 enter as visitor (read-only)",
            "  logout                                end the current session",
            "  whoami                                show current user",
            "  find <laptops|ovens|all> [price=min;max]",
            "  cost <laptops|ovens|all>",
            "  cheapest                              show the cheapest appliance in stock",
            "  add laptop <name> <weight> <price> <quantity> <os> <cpu> <battery>",
            "  add oven <name> <weight> <price> <quantity> <power> <capacity>",
            "  remove laptop <id>",
            "  remove oven <id>",
            "  users list                            (admin)",
            "  users add <username> <password> <role>  (admin)",
            "  users delete <id>                     (admin)",
            "  help",
            "  exit");

    @Override
    public boolean requiresAuth() {
        return false;
    }

    @Override
    public Response execute(Request request) {
        return new ResponseImpl(HELP);
    }
}
