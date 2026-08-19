package com.adxam.warehouse.controller.command;

import com.adxam.warehouse.controller.Response;
import com.adxam.warehouse.controller.ResponseImpl;

public class HelpCommand implements Command {

    private static final String MENU = """
            Available commands:
              find <laptops|ovens|all> [parameter=value]... [sort=field asc|desc]
              cost <laptops|ovens|all>
              help
              exit

            Search parameters:
              any product : id, name, price, weight, quantity
              laptops only: os, cpu, battery
              ovens only  : power, capacity
              numbers accept an exact value or a min;max range

            Sort fields:
              id, name, category, price, quantity, weight   (default order: asc)

            Examples:
              find all
              find all sort=price desc
              find laptops cpu=Intel i5
              find laptops os=LINUX sort=name
              find ovens power=2000;2300 sort=weight desc
              find all price=600;1600 sort=category
              cost all""";

    @Override
    public Response execute(String[] args) {
        return new ResponseImpl(MENU);
    }
}
