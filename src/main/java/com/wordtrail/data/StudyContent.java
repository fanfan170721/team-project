package com.wordtrail.data;
import com.wordtrail.entity.StudyState.Question;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/** Offline English curriculum with stable legacy identifiers and explicit source metadata. */
public final class StudyContent {
    private StudyContent() { }
    public static List<Question> load() {
        try (InputStream input = StudyContent.class.getResourceAsStream("/english-study-content.json")) {
            JSONObject document = new JSONObject(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            if (document.getInt("version") != 1) { throw new IllegalArgumentException("Unsupported curriculum."); }
            JSONArray rows = document.getJSONArray("words");
            List<Question> result = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            Set<String> terms = new HashSet<>();
            for (Object item : rows) {
                JSONObject row = (JSONObject) item;
                if (!ids.add(row.getString("id")) || !terms.add(row.getString("term").toLowerCase(java.util.Locale.ROOT))
                        || row.getString("source").isBlank()) {
                    throw new IllegalArgumentException("Duplicate or unattributed curriculum word.");
                }
                List<String> options = new ArrayList<>();
                options.add(row.getString("definition"));
                for (Object candidate : rows) {
                    String alternative = ((JSONObject) candidate).getString("definition");
                    if (!options.contains(alternative) && options.size() < 4) { options.add(alternative); }
                    if (options.size() == 4) { break; }
                }
                result.add(new Question(row.getString("id"), row.getString("term"), row.getString("definition"),
                        row.getInt("level"), options));
            }
            return List.copyOf(result);
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot read the bundled English curriculum.", failure);
        }
    }
}
