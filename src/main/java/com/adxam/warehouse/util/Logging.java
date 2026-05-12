package com.adxam.warehouse.util;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class Logging {

    private Logging() {}

    public static void configure(String levelName) {
        Level level;
        try {
            level = Level.parse(levelName == null ? "INFO" : levelName.toUpperCase());
        } catch (IllegalArgumentException e) {
            level = Level.INFO;
        }

        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) {
            root.removeHandler(h);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        handler.setFormatter(new ShortFormatter());
        root.addHandler(handler);
        root.setLevel(level);
    }

    private static class ShortFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String thread = "thread-" + record.getLongThreadID();
            return String.format("[%1$tT %2$s %3$s %4$s] %5$s%n",
                    new Date(record.getMillis()),
                    record.getLevel().getName(),
                    thread,
                    record.getLoggerName(),
                    formatMessage(record));
        }
    }
}
