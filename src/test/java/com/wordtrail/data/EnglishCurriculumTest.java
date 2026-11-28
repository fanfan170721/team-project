package com.wordtrail.data;
import com.wordtrail.entity.StudyState.Question;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnglishCurriculumTest {
    @Test void bankIsEnglishDiverseAndKeepsEveryLegacyIdentifierAndMeaning() throws Exception {
        List<Question> bank=StudyContent.load();
        assertEquals(1128,bank.size());
        assertEquals(bank.size(),bank.stream().map(q->q.term.toLowerCase()).distinct().count());
        for(int level=1;level<=15;level++) {
            int chosen=level;
            assertTrue(bank.stream().filter(q->q.level==chosen).count()>=20,"Level "+level);
        }
        for(Question word:bank) {
            assertFalse(word.definition.matches(".*[\\u3400-\\u9fff].*"));
            assertTrue(word.definition.length()>5);
        }
        try(java.io.BufferedReader reader=new java.io.BufferedReader(new java.io.InputStreamReader(
                getClass().getResourceAsStream("/study-content.csv"),java.nio.charset.StandardCharsets.UTF_8))) {
            reader.readLine(); String row; int checked=0;
            while((row=reader.readLine())!=null) {
                String[] fields=row.split(",",-1);
                Question word=bank.stream().filter(q->q.id.equals("reference-"+fields[0])).findFirst().orElseThrow();
                assertEquals(fields[1],word.term); assertEquals(fields[2],word.definition); checked++;
            }
            assertEquals(60,checked);
        }
    }
    @org.junit.jupiter.api.io.TempDir java.nio.file.Path directory;
    @Test void placementSamplesAllFifteenLevelsAndImmediatelyStartsAtTheAdoptedLevel() throws Exception {
        try(com.wordtrail.app.Application app=new com.wordtrail.app.Application(directory)) {
            app.controller().setup("allen","password123".toCharArray());
            app.controller().study().goal(42); app.controller().study().start();
            com.wordtrail.adapter.StudyViewModel view=app.controller().study().assess();
            for(int i=0;i<20;i++) {
                view=app.controller().study().answer(view.assessment.token,
                        view.assessment.options.indexOf(view.assessment.definition),true);
            }
            assertTrue(view.assessment.result.contains("L15"));
            app.controller().study().adopt(view.assessment.token);
            assertEquals(42,app.controller().study().show().goal);
        }
        try(JsonFileStore store=new JsonFileStore(directory)) {
            assertEquals(15,store.getStudy("allen").assessment.questions.stream().map(q->q.level).distinct().count());
            assertTrue(store.getStudy("allen").round.questions.stream().allMatch(q->q.level==15));
            assertEquals(0,store.getStudy("allen").learned);
        }
    }
}
