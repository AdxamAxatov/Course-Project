package com.adxam.warehouse;

import com.adxam.warehouse.controller.ControllerFactory;
import com.adxam.warehouse.controller.RequestImpl;
import com.adxam.warehouse.controller.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpCommandTest {

    @BeforeAll
    static void configureFactories() {
        TestFactories.initWithTestResources();
    }

    @Test
    void helpListsEveryCommand() {
        String menu = execute("help").responseString();

        assertTrue(menu.contains("find"), menu);
        assertTrue(menu.contains("cost"), menu);
        assertTrue(menu.contains("help"), menu);
        assertTrue(menu.contains("exit"), menu);
    }

    /** Every example the menu advertises must actually work, so the help text cannot drift. */
    @Test
    void everyAdvertisedExampleRuns() {
        List<String> examples = examplesFromMenu(execute("help").responseString());

        assertFalse(examples.isEmpty(), "no examples were found in the menu");

        for (String example : examples) {
            Response response = execute(example);
            assertTrue(response.isOk(),
                    "example from the menu failed: '" + example + "' -> " + response.responseString());
        }
    }

    private List<String> examplesFromMenu(String menu) {
        List<String> examples = new ArrayList<>();
        boolean inExamples = false;

        for (String line : menu.split("\\R")) {
            if (line.strip().equals("Examples:")) {
                inExamples = true;
                continue;
            }
            if (inExamples && !line.isBlank()) {
                examples.add(line.strip());
            }
        }
        return examples;
    }

    private Response execute(String command) {
        return ControllerFactory.getInstance().execute(new RequestImpl(command));
    }
}
