package com.wordtrail.usecase;

import com.wordtrail.entity.QuizQuestion;
import com.wordtrail.entity.VocabularyWord;
import java.util.List;

public interface ContentUseCase {
    VocabularyWord addWord(String term, String topic, String definition, String example, String source);
    QuizQuestion addQuestion(String wordId, String prompt, List<String> options, int correctIndex);
}
