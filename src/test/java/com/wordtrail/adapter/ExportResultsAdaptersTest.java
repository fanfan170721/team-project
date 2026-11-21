package com.wordtrail.adapter;

import com.wordtrail.usecase.ExportResultsResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportResultsAdaptersTest {
    private final ExportResultsViewModel viewModel = new ExportResultsViewModel();
    private final ExportResultsPresenter presenter = new ExportResultsPresenter(viewModel);
    private final List<String> received = new ArrayList<>();

    private ExportResultsController controllerThat(java.util.function.Consumer<String> behaviour) {
        return new ExportResultsController(destination -> {
            received.add(destination);
            behaviour.accept(destination);
        }, viewModel);
    }

    @Test
    void successReportsDestinationAndPluralCount() {
        ExportResultsController controller = controllerThat(
                destination -> presenter.prepareSuccessView(new ExportResultsResponse(destination, 3)));

        ExportResultsViewModel.Result result = controller.export("C:/exports/results.csv");

        assertEquals(List.of("C:/exports/results.csv"), received);
        assertTrue(result.success);
        assertEquals("C:/exports/results.csv", result.destination);
        assertEquals(3, result.exportedCount);
        assertEquals("Exported 3 quiz results to C:/exports/results.csv.", result.message);
    }

    @Test
    void singleAndZeroResultsUseClearWording() {
        assertEquals("Exported 1 quiz result to one.csv.", controllerThat(
                d -> presenter.prepareSuccessView(new ExportResultsResponse(d, 1))).export("one.csv").message);

        ExportResultsViewModel.Result empty = controllerThat(
                d -> presenter.prepareSuccessView(new ExportResultsResponse(d, 0))).export("empty.csv");
        assertTrue(empty.success);
        assertEquals(0, empty.exportedCount);
        assertEquals("Exported 0 quiz results to empty.csv. The file contains only the header row.", empty.message);
    }

    @Test
    void failureIsPresentedWithoutSuccessData() {
        ExportResultsViewModel.Result result = controllerThat(
                d -> presenter.prepareFailView("Export failed: Disk is full.")).export("results.csv");

        assertFalse(result.success);
        assertEquals("", result.destination);
        assertEquals(0, result.exportedCount);
        assertEquals("Export failed: Disk is full.", result.message);
    }

    @Test
    void eachRequestClearsThePreviousResult() {
        controllerThat(d -> presenter.prepareSuccessView(new ExportResultsResponse(d, 2))).export("first.csv");

        ExportResultsController silent = controllerThat(d -> { });
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> silent.export("second.csv"));
        assertEquals("The export request did not produce a result.", error.getMessage());
    }
}
