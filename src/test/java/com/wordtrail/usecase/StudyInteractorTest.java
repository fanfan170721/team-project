package com.wordtrail.usecase;

import com.wordtrail.data.JsonFileStore;
import com.wordtrail.data.StudyContent;
import com.wordtrail.entity.LearningProgress;
import com.wordtrail.entity.Role;
import com.wordtrail.entity.StudyState;
import com.wordtrail.entity.StudyState.Question;
import com.wordtrail.entity.StudyState.Round;
import com.wordtrail.entity.User;
import com.wordtrail.usecase.port.StudyRepository;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Business invariants against real atomic JSON persistence, including injected failed writes. */
class StudyInteractorTest {
    @TempDir Path directory;
    private JsonFileStore store;
    private final SessionContext session = new SessionContext();
    private StudyInteractor study;
    private StudyState shown;
    private boolean failSave;
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-27T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach void open() {
        store = new JsonFileStore(directory);
        User user = new User("allen", "hash", Role.LEARNER);
        store.createUser(user);
        session.signIn(user);
        wire();
    }

    private void wire() {
        StudyRepository boundary = new StudyRepository() {
            public StudyState getStudy(String user) { return store.getStudy(user); }
            public void saveStudy(String user, StudyState state, LearningProgress progress) {
                if (failSave) { throw new AppException("Simulated disk full."); }
                store.saveStudy(user, state, progress);
            }
        };
        study = new StudyInteractor(StudyContent.load().subList(0, 60), boundary, store, session, clock, new Random(7),
                (state, feedback) -> shown = state);
    }

    @AfterEach void close() throws Exception { store.close(); }
    private Round round() { return store.getStudy("allen").round; }
    private Round assessment() { return store.getStudy("allen").assessment; }

    private void learnToTest() {
        while (!round().phase.equals("TEST") && !round().phase.equals("COMPLETED")) {
            study.advance(round().token());
        }
    }

    private void correct(boolean placement) {
        Round value = placement ? assessment() : round();
        Question question = value.questions.get(value.pending.get(value.position));
        study.answer(value.token(), question.options.indexOf(question.definition), placement);
    }

    private void complete() {
        learnToTest();
        while (round().phase.equals("TEST")) { correct(false); }
    }

    @Test void resumesExactQuestionsAndKeepsMistakesThroughSingleWordRetestsAndCompletion() throws Exception {
        study.show();
        assertNull(shown.round);
        study.start();
        String id = round().id;
        study.start();
        assertEquals(id, shown.round.id);
        learnToTest();
        Round before = round();
        Question first = before.questions.get(0);
        study.answer(before.token(), 4, false);
        assertThrows(AppException.class, () -> study.answer(before.token(), 4, false));
        store.close();
        store = new JsonFileStore(directory);
        wire();
        study.show();
        assertEquals(id, shown.round.id);
        assertEquals(1, shown.round.position);
        assertEquals(first.options, shown.round.questions.get(0).options);
        while (round().phase.equals("TEST")) { correct(false); }
        assertEquals("REVIEW", round().phase);
        assertEquals(List.of(0), round().pending);
        assertEquals(0, store.getStudy("allen").mastered.size());
        assertEquals(0, store.getProgress("allen").getAttempts().size());
        learnToTest();
        int wrong = (first.options.indexOf(first.definition) + 1) % 4;
        study.answer(round().token(), wrong, false);
        learnToTest();
        assertEquals(first.options, round().questions.get(0).options);
        correct(false);
        StudyState done = store.getStudy("allen");
        assertEquals("COMPLETED", done.round.phase);
        assertEquals(20, done.mastered.size());
        assertEquals(20, done.learned);
        assertEquals(20, done.practiced);
        assertArrayEquals(new int[]{1, 1, 2}, done.mistakes.get(first.id));
        assertEquals(1, done.history.size());
        LearningProgress progress = store.getProgress("allen");
        assertEquals(20, progress.getPersonalWords().size());
        assertEquals(20, progress.getStudiedWordIds().size());
        assertTrue(progress.getReviewWordIds().contains("guided-" + first.id));
        assertEquals(19, progress.getAttempts().get(0).getCorrect());
        assertThrows(AppException.class, () -> study.advance(round().token()));
        assertThrows(AppException.class, () -> study.answer(round().token(), 0, false));
        assertEquals(1, store.getProgress("allen").getAttempts().size());
    }

    @Test void knownWordsDoNotInflateNetNewGoalsAndReplayNeverInflatesPractice() {
        study.goal(50);
        study.start();
        String token = round().token();
        study.known(token, 0, true);
        study.known(token, 0, false);
        for (int index = 0; index < 5; index++) { study.known(token, index, true); }
        complete();
        StudyState done = store.getStudy("allen");
        assertEquals(20, done.mastered.size());
        assertEquals(15, done.learned);
        assertEquals(15, done.round.netNew);
        assertEquals(15, store.getProgress("allen").getStudiedWordIds().size());
        study.goal(10);
        assertEquals(15, shown.goalBaseline);
        assertThrows(AppException.class, () -> study.replay("missing"));
        study.replay(done.round.id);
        assertEquals("REPLAY", round().mode);
        complete();
        StudyState replayed = store.getStudy("allen");
        assertEquals(2, replayed.history.size());
        assertEquals(15, replayed.learned);
        assertEquals(20, replayed.practiced);
        assertEquals(1, store.getProgress("allen").getAttempts().size());
        study.start();
        complete();
        assertEquals(35, store.getStudy("allen").learned);
        assertEquals(40, store.getStudy("allen").practiced);
        assertEquals(40, store.getProgress("allen").getPersonalWords().size());
        study.start();
        complete();
        study.start();
        complete();
        assertEquals(55, store.getStudy("allen").learned);
        assertEquals(60, store.getProgress("allen").getPersonalWords().size());
    }

    @Test void everyKnownWordCanCompleteWithoutAnswerAndDoubleCompletionIsRejected() {
        study.start();
        for (int index = 0; index < 20; index++) { study.known(round().token(), index, true); }
        study.advance(round().token());
        assertEquals("COMPLETED", round().phase);
        assertEquals(0, store.getStudy("allen").learned);
        assertEquals(20, store.getStudy("allen").mastered.size());
        assertEquals(20, store.getProgress("allen").getAttempts().get(0).getCorrect());
        assertThrows(AppException.class, () -> study.advance(round().token()));
    }

    @Test void assessmentResumesAndAdoptionImmediatelyReplacesUnfinishedWorkExactlyOnce() throws Exception {
        study.start();
        complete();
        study.goal(70);
        study.start();
        String original = round().id;
        study.advance(round().token());
        study.assess();
        String assessmentId = assessment().id;
        correct(true);
        store.close();
        store = new JsonFileStore(directory);
        wire();
        study.assess();
        assertEquals(assessmentId, shown.assessment.id);
        assertEquals(1, shown.assessment.position);
        assertThrows(AppException.class, () -> study.adopt(assessment().token()));
        while (assessment().phase.equals("TEST")) { correct(true); }
        String adoptionToken = assessment().token();
        assertEquals(3, assessment().level);
        study.adopt(adoptionToken);
        StudyState adopted = store.getStudy("allen");
        assertNotEquals(original, adopted.round.id);
        assertEquals("OVERVIEW", adopted.round.phase);
        assertTrue(adopted.round.questions.stream().allMatch(q -> q.level == 3));
        assertEquals(20, adopted.mastered.size());
        assertEquals(70, adopted.goalTarget);
        assertEquals(20, adopted.goalBaseline);
        assertEquals("SUPERSEDED", adopted.history.get(1).phase);
        assertEquals(1, store.getProgress("allen").getAttempts().size());
        study.adopt(adoptionToken);
        assertEquals(adopted.round.id, shown.round.id);
        study.assess();
        assertNotEquals(assessmentId, assessment().id);
        for (int index = 0; index < 20; index++) { study.answer(assessment().token(), 4, true); }
        assertEquals(1, assessment().level);
        assertTrue(store.getStudy("allen").mistakes.isEmpty());
    }

    @Test void failedSaveChangesNeitherRoundNorCompletionAndDoesNotPublishSuccess() {
        study.start();
        learnToTest();
        for (int index = 0; index < 19; index++) { correct(false); }
        String token = round().token();
        failSave = true;
        assertThrows(AppException.class, () -> correct(false));
        assertEquals(token, round().token());
        assertEquals(0, store.getStudy("allen").mastered.size());
        assertTrue(store.getProgress("allen").getAttempts().isEmpty());
        assertEquals("TEST", shown.round.phase);
        failSave = false;
        correct(false);
        assertEquals(1, store.getProgress("allen").getAttempts().size());
    }

    @Test void authenticationAccountIsolationAndInvalidActionsAreEnforced() {
        assertThrows(AppException.class, () -> study.advance("stale"));
        assertThrows(AppException.class, () -> study.adopt("stale"));
        assertThrows(AppException.class, () -> study.goal(0));
        study.start();
        String token = round().token();
        assertThrows(AppException.class, () -> study.known(token, 20, true));
        assertThrows(AppException.class, () -> study.answer(token, 0, false));
        assertThrows(AppException.class, () -> study.replay("missing"));
        learnToTest();
        assertThrows(AppException.class, () -> study.known(round().token(), 0, true));
        assertThrows(AppException.class, () -> study.answer(round().token(), -1, false));
        User bob = new User("bob", "hash", Role.LEARNER);
        store.createUser(bob);
        session.signIn(bob);
        study.show();
        assertNull(shown.round);
        assertThrows(AppException.class, () -> study.answer(token, 0, false));
        study.start();
        assertNotEquals(round().id, shown.round.id);
        session.signOut();
        assertThrows(AppException.class, study::show);
    }

    @Test void rejectsIncompleteContentAndCopiesSnapshotCollections() {
        assertThrows(IllegalArgumentException.class, () -> new StudyInteractor(List.of(), store, store,
                session, clock, new Random(), (state, feedback) -> { }));
        assertThrows(IllegalArgumentException.class, () -> new Question("", "a", "b", 1, List.of("b")));
        Question same = new Question("id", "term", "meaning", 1, List.of("meaning", "b", "c", "d"));
        StudyInteractor badBank = new StudyInteractor(java.util.Collections.nCopies(20, same), store, store,
                session, clock, new Random(), (state, feedback) -> { });
        assertThrows(AppException.class, badBank::start);
        study.start();
        study.assess();
        StudyState copy = new StudyState(store.getStudy("allen"));
        copy.round.questions.clear();
        copy.assessment.questions.clear();
        assertEquals(20, round().questions.size());
        assertEquals(20, assessment().questions.size());
    }

    @Test void privateDefinitionsAffectNewQuestionsButNeverChangeTheActiveSnapshot() {
        study.start();
        complete();
        Question original = round().questions.get(0);
        String id = "guided-" + original.id;
        store.saveProgress("allen", store.getProgress("allen").withDefinition(id, "My private meaning."));
        study.assess();
        for (int index = 0; index < 20; index++) { study.answer(assessment().token(), 4, true); }
        study.adopt(assessment().token());
        Question snapshot = round().questions.stream().filter(q -> q.id.equals(original.id)).findFirst().orElseThrow();
        assertEquals("My private meaning.", snapshot.definition);
        assertTrue(snapshot.options.contains("My private meaning."));
        store.saveProgress("allen", store.getProgress("allen").withDefinition(id, "A later edit."));
        study.show();
        assertEquals("My private meaning.", shown.round.questions.stream()
                .filter(q -> q.id.equals(original.id)).findFirst().orElseThrow().definition);
        complete();
        assertEquals("A later edit.", store.getProgress("allen").getDefinitions().get(id));
    }
}
