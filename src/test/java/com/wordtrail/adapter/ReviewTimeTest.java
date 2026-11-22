package com.wordtrail.adapter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ReviewTimeTest {
    @Test
    void equalityAndOrderingUseTheInstantIndependentlyOfDisplayText() {
        Instant instant = Instant.parse("2026-11-01T05:30:00Z");
        ViewModels.ReviewTime first = new ViewModels.ReviewTime(instant, "01:30 EDT");
        ViewModels.ReviewTime sameInstant = new ViewModels.ReviewTime(instant, "05:30 UTC");
        ViewModels.ReviewTime repeatedLocalTime = new ViewModels.ReviewTime(
                Instant.parse("2026-11-01T06:30:00Z"), "01:30 EST");
        List<ViewModels.ReviewTime> values = List.of(first, sameInstant, repeatedLocalTime);

        assertEquals(first, sameInstant);
        assertEquals(sameInstant, first);
        assertEquals(first.hashCode(), sameInstant.hashCode());
        assertEquals(0, first.compareTo(sameInstant));
        assertNotEquals(first, repeatedLocalTime);
        assertEquals(2, new HashSet<>(values).size());
        assertEquals(new HashSet<>(values), new TreeSet<>(values));
        assertEquals("01:30 EDT", first.toString());
        assertEquals("05:30 UTC", sameInstant.toString());
    }

    @Test
    void neverReviewedValuesHaveOneIdentityAndSortBeforeDatedReviews() {
        ViewModels.ReviewTime first = new ViewModels.ReviewTime(null, "Never reviewed");
        ViewModels.ReviewTime another = new ViewModels.ReviewTime(null, "");
        ViewModels.ReviewTime dated = new ViewModels.ReviewTime(Instant.EPOCH, "1970-01-01");
        List<ViewModels.ReviewTime> values = List.of(dated, first, another);

        assertEquals(first, first);
        assertEquals(first, another);
        assertEquals(first.hashCode(), another.hashCode());
        assertEquals(0, first.compareTo(another));
        assertEquals(first, new TreeSet<>(values).first());
        assertEquals(new HashSet<>(values), new TreeSet<>(values));
        assertNotEquals(first, dated);
        assertNotEquals(first, null);
        assertNotEquals(first, "Never reviewed");
    }
}
