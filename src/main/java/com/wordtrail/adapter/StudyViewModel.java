package com.wordtrail.adapter;

import java.util.List;

/** Display data only; Swing never sees the mutable persistence snapshots. */
public final class StudyViewModel {
    public String summary;
    public String feedback;
    public String mistakes;
    public int goal;
    public RoundView round;
    public RoundView assessment;
    public List<HistoryRow> history;

    public static final class RoundView {
        public String token;
        public String phase;
        public String mode;
        public String heading;
        public String word;
        public String definition;
        public String result;
        public boolean adopted;
        public List<String> options;
        public List<Object[]> overview;
    }

    public static final class HistoryRow {
        public final String id;
        public final Object[] cells;
        public final boolean completed;
        public HistoryRow(String id, Object[] cells, boolean completed) {
            this.id = id;
            this.cells = cells;
            this.completed = completed;
        }
    }
}
