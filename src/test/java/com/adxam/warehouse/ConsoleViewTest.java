package com.adxam.warehouse;

import com.adxam.warehouse.view.ConsoleViewImpl;
import com.adxam.warehouse.view.View;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleViewTest {

    @BeforeAll
    static void configureFactories() {
        TestFactories.initWithTestResources();
    }

    @Test
    void viewStopsCleanlyWhenInputEndsWithoutExitCommand() {
        String output = runViewWith("bogus\n");

        assertTrue(output.contains("Wrong command"), output);
    }

    @Test
    void viewPrintsTheMenuOnStartup() {
        String output = runViewWith("exit\n");

        assertTrue(output.contains("Available commands:"), output);
    }

    @Test
    void viewStopsCleanlyOnEmptyInput() {
        assertDoesNotThrow(() -> runViewWith(""));
    }

    @Test
    void viewStopsOnExitCommand() {
        String output = runViewWith("exit\n");

        assertTrue(output.contains("Great job!"), output);
    }

    private String runViewWith(String input) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));

            View view = new ConsoleViewImpl();
            view.start();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        return captured.toString(StandardCharsets.UTF_8);
    }
}
