package com.adxam.warehouse.controller.command;

import java.util.Map;

public class CommandProvider {
    private static final Map<String, Command> REPOSITORY = Map.ofEntries(
            Map.entry("login", new LoginCommand()),
            Map.entry("logout", new LogoutCommand()),
            Map.entry("visit", new VisitCommand()),
            Map.entry("whoami", new WhoamiCommand()),
            Map.entry("help", new HelpCommand()),
            Map.entry("find", new FindCommand()),
            Map.entry("cost", new CostCommand()),
            Map.entry("cheapest", new CheapestCommand()),
            Map.entry("add", new AddCommand()),
            Map.entry("remove", new RemoveCommand()),
            Map.entry("delete", new RemoveCommand()),
            Map.entry("users", new UsersCommand()),
            Map.entry("exit", new ExitCommand()),
            Map.entry("quit", new ExitCommand())
    );

    private static final Command FALLBACK = new WrongCommand();

    public static Command getCommand(String name) {
        if (name == null) return FALLBACK;
        return REPOSITORY.getOrDefault(name.toLowerCase(), FALLBACK);
    }
}
