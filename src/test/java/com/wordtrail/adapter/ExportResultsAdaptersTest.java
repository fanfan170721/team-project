package com.wordtrail.adapter;

import com.wordtrail.usecase.ExportResultsInputBoundary;
import com.wordtrail.usecase.ExportResultsResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportResultsAdaptersTest {
    private final ExportResultsViewModel viewModel = new ExportResultsViewModel();
    private final ExportResultsPresenter presenter = new ExportResultsPresenter(viewModel);
    private final List<String> received = new ArrayList<>();
    /** What the scripted use case does for each successive request. */
    private final Deque<Consumer<String>> script = new ArrayDeque<>();
    private final ExportResultsInputBoundary useCase = destination -> {
        received.add(destination);
        script.removeFirst().accept(destination);
    };
    private final ExportResultsController controller = new ExportResultsController(useCase, viewModel);

    private void thenSucceeds(int count) {
        script.add(destination -> presenter.prepareSuccessView(new ExportResultsResponse(destination, count)));
    }

    private void thenFails(String message) {
        script.add(destination -> presenter.prepareFailView(message));
    }

    @Test
    void passesTheDestinationUnchangedAndCallsTheUseCaseOnce() {
        String destination = "  C:/My exports/résultats, 2026.csv ";
        thenSucceeds(1);

        controller.export(destination);

        assertEquals(List.of(destination), received);
        assertTrue(script.isEmpty());
    }

    @Test
    void successReportsDestinationAndCount() {
        thenSucceeds(3);

        ExportResultsViewModel.Result result = controller.export("C:/exports/results.csv");

        assertTrue(result.success);
        assertEquals("C:/exports/results.csv", result.destination);
        assertEquals(3, result.exportedCount);
        assertEquals("Exported 3 quiz results to C:/exports/results.csv.", result.message);
    }

    @Test
    void singleResultUsesSingularWording() {
        thenSucceeds(1);

        assertEquals("Exported 1 quiz result to one.csv.", controller.export("one.csv").message);
    }

    @Test
    void zeroResultsIsAnExplicitSuccess() {
        thenSucceeds(0);

        ExportResultsViewModel.Result empty = controller.export("empty.csv");

        assertTrue(empty.success);
        assertEquals("empty.csv", empty.destination);
        assertEquals(0, empty.exportedCount);
        assertEquals("Exported 0 quiz results to empty.csv. The file contains only the header row.", empty.message);
    }

    @Test
    void failureAfterSuccessKeepsOnlyTheNewError() {
        thenSucceeds(2);
        thenFails("Export failed: Disk is full.");
        ExportResultsViewModel.Result first = controller.export("first.csv");

        ExportResultsViewModel.Result failed = controller.export("second.csv");

        assertFalse(failed.success);
        assertEquals("", failed.destination);
        assertEquals(0, failed.exportedCount);
        assertEquals("Export failed: Disk is full.", failed.message);
        assertTrue(first.success, "the earlier result object is not changed by a later request");
        assertEquals("first.csv", first.destination);
        assertEquals(2, first.exportedCount);
        assertEquals("Exported 2 quiz results to first.csv.", first.message);
    }

    @Test
    void successAfterFailureKeepsNoOldError() {
        thenFails("Please choose where to save the exported results.");
        thenSucceeds(4);
        ExportResultsViewModel.Result failed = controller.export(" ");

        ExportResultsViewModel.Result result = controller.export("retry.csv");

        assertTrue(result.success);
        assertEquals("Exported 4 quiz results to retry.csv.", result.message);
        assertFalse(failed.success);
        assertEquals("Please choose where to save the exported results.", failed.message);
    }

    @Test
    void aRequestWithoutOutputNeverReusesThePreviousResult() {
        thenSucceeds(2);
        script.add(destination -> { });
        controller.export("first.csv");

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> controller.export("second.csv"));

        assertEquals("The export request did not produce a result.", error.getMessage());
        assertEquals(List.of("first.csv", "second.csv"), received);
    }

    @Test
    void resultIsImmutableDisplayDataOnly() {
        for (Field field : ExportResultsViewModel.Result.class.getDeclaredFields()) {
            assertTrue(Modifier.isFinal(field.getModifiers()), field.getName());
            Class<?> type = field.getType();
            assertTrue(type == boolean.class || type == int.class || type == String.class, field.getName());
        }
    }
}
