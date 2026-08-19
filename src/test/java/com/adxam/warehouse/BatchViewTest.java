package com.adxam.warehouse;

import com.adxam.warehouse.view.BatchViewImpl;
import com.adxam.warehouse.view.View;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchViewTest {

    @BeforeAll
    static void configureFactories() {
        TestFactories.initWithTestResources();
    }

    @Test
    void batchViewRunsEveryCommandFromTheScript() {
        String output = runBatch("commands-test.txt");

        assertTrue(output.contains("> help"), output);
        assertTrue(output.contains("Available commands:"), output);
        assertTrue(output.contains("Total inventory value for all: $11900"), output);
        assertTrue(output.contains("Great job!"), output);
    }

    @Test
    void batchViewStopsAtExitAndIgnoresTheRemainingLines() {
        String output = runBatch("commands-test.txt");

        assertFalse(output.contains("Total inventory value for laptops"), output);
    }

    @Test
    void batchViewFailsLoudlyWhenTheScriptIsMissing() {
        View view = new BatchViewImpl("no-such-script.txt");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, view::start);

        assertTrue(thrown.getMessage().contains("no-such-script.txt"), thrown.getMessage());
    }

    private String runBatch(String scriptName) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new BatchViewImpl(scriptName).start();
        } finally {
            System.setOut(originalOut);
        }

        return captured.toString(StandardCharsets.UTF_8);
    }
}
