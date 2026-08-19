package com.adxam.warehouse;

import com.adxam.warehouse.controller.Controller;
import com.adxam.warehouse.controller.ControllerFactory;
import com.adxam.warehouse.controller.RequestImpl;
import com.adxam.warehouse.controller.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the find command through the controller, the way the view does,
 * so parsing, searching, sorting and rendering are all covered.
 */
class SearchCommandTest {

    private static final Pattern PRODUCT_ID = Pattern.compile("^([LO]-\\d+)", Pattern.MULTILINE);

    @BeforeAll
    static void configureFactories() {
        TestFactories.initWithTestResources();
    }

    @Test
    void findAllReturnsEveryProductWithItsDetails() {
        String output = run("find all");

        assertTrue(output.contains("ID"), output);
        assertTrue(output.contains("NAME"), output);
        assertTrue(output.contains("CATEGORY"), output);
        assertTrue(output.contains("PRICE"), output);
        assertTrue(output.contains("QTY"), output);
        assertTrue(output.contains("6 product(s) found."), output);
        assertEquals(6, idsIn(output).size(), output);
    }

    @Test
    void findSortsAscendingByPrice() {
        assertEquals(List.of("O-101", "O-102", "L-101", "O-103", "L-103", "L-102"),
                idsIn(run("find all sort=price")));
    }

    @Test
    void findSortsDescendingByPrice() {
        assertEquals(List.of("L-102", "L-103", "O-103", "L-101", "O-102", "O-101"),
                idsIn(run("find all sort=price desc")));
    }

    @Test
    void findSortsByNameAndByQuantity() {
        assertEquals(List.of("O-101", "O-102", "O-103"), idsIn(run("find ovens sort=name")));
        assertEquals(List.of("O-102", "O-101", "O-103"), idsIn(run("find ovens sort=quantity")));
    }

    @Test
    void findAcceptsAParameterValueContainingSpaces() {
        assertEquals(List.of("L-102"), idsIn(run("find laptops cpu=Intel i5")));
    }

    @Test
    void findFiltersOnASubtypeSpecificParameter() {
        assertEquals(List.of("L-103"), idsIn(run("find laptops os=LINUX")));
    }

    @Test
    void findFiltersOnANumericRange() {
        assertEquals(List.of("L-103", "L-102"), idsIn(run("find laptops price=1000;1400 sort=price")));
    }

    @Test
    void findCombinesSeveralParameters() {
        assertEquals(List.of("O-103"), idsIn(run("find ovens capacity=35;45 power=2100;2400")));
    }

    @Test
    void findAppliesTheFilterToACategorySearchInsteadOfIgnoringIt() {
        String output = run("find laptops price=1;2");

        assertTrue(output.contains("No matching products were found."), output);
        assertTrue(idsIn(output).isEmpty(), output);
    }

    @Test
    void findReportsWhenNothingMatches() {
        assertTrue(run("find all price=1;2").contains("No matching products were found."));
    }

    @Test
    void findRejectsAParameterThatDoesNotBelongToTheCategory() {
        assertTrue(failing("find laptops power=2000").contains("only available for ovens"));
        assertTrue(failing("find ovens cpu=Intel").contains("only available for laptops"));
    }

    @Test
    void findRejectsAnUnknownParameter() {
        assertTrue(failing("find all colour=red").contains("Unknown parameter: 'colour'"));
    }

    @Test
    void findRejectsAParameterGivenTwice() {
        assertTrue(failing("find all name=Test name=Other").contains("more than once"));
    }

    @Test
    void findRejectsAParameterWithoutAValue() {
        assertTrue(failing("find all name=").contains("has no value"));
    }

    @Test
    void findReportsANonNumericValueInsteadOfLeakingAJavaException() {
        String message = failing("find all price=abc");

        assertTrue(message.contains("Parameter 'price' expects a number or a min;max range"), message);
        assertFalse(message.contains("For input string"), message);
    }

    @Test
    void findReportsAMalformedRangeInsteadOfLeakingAJavaException() {
        String message = failing("find all price=100;200;300");

        assertTrue(message.contains("expects a number or a min;max range"), message);
        assertFalse(message.contains("Index"), message);
    }

    @Test
    void findRejectsAnUnknownSortField() {
        assertTrue(failing("find all sort=colour").contains("Cannot sort by 'colour'"));
    }

    @Test
    void findRejectsAnUnknownSortOrder() {
        assertTrue(failing("find all sort=price sideways").contains("Sort order must be 'asc' or 'desc'"));
    }

    @Test
    void findRejectsAnUnknownCategory() {
        assertTrue(failing("find gadgets").contains("Unknown category"));
    }

    @Test
    void findRejectsATokenThatIsNotAParameter() {
        assertTrue(failing("find all cheap").contains("Expected parameter=value"));
    }

    @Test
    void findWithoutACategoryExplainsTheFormat() {
        assertTrue(failing("find").contains("Format: find"));
    }

    private String run(String command) {
        Response response = controller().execute(new RequestImpl(command));
        assertTrue(response.isOk(), "expected a successful response, got: " + response.responseString());
        return response.responseString();
    }

    private String failing(String command) {
        Response response = controller().execute(new RequestImpl(command));
        assertFalse(response.isOk(), "expected a failed response, got: " + response.responseString());
        return response.responseString();
    }

    private Controller controller() {
        return ControllerFactory.getInstance();
    }

    private List<String> idsIn(String output) {
        List<String> ids = new ArrayList<>();
        Matcher matcher = PRODUCT_ID.matcher(output);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }
}
