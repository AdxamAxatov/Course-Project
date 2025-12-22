package com.adxam.warehouse.controller.command;
import java.util.Map;
public class CommandProvider {
    private static final Map<String, Command> REPOSITORY = Map.of(
        "find", new FindCommand(),
        "cost", new CostCommand(),
        "exit", new ExitCommand()
    );
    public static Command getCommand(String name) {
        return REPOSITORY.getOrDefault(name.toLowerCase(), new WrongCommand());
    }
}
