package com.wordtrail.usecase.port;

import com.wordtrail.entity.QuizQuestion;
import com.wordtrail.entity.VocabularyWord;
import java.util.List;

public interface ContentRepository {
    List<VocabularyWord> getWords();
    List<QuizQuestion> getQuestions();
    void addWord(VocabularyWord word);
    void addQuestion(QuizQuestion question);
}
