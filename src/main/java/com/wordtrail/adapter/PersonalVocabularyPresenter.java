package com.wordtrail.adapter;

import com.wordtrail.entity.LearningProgress;
import com.wordtrail.usecase.PersonalVocabularyOutputBoundary;
import com.wordtrail.usecase.PersonalVocabularyResponse;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Presents edit outcomes and formats account-specific vocabulary metadata. */
public final class PersonalVocabularyPresenter implements PersonalVocabularyOutputBoundary {
    private final DateTimeFormatter reviewTime;
    private final PersonalVocabularyViewModel viewModel;

    public PersonalVocabularyPresenter(ZoneId zone, PersonalVocabularyViewModel viewModel) {
        reviewTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zone);
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(PersonalVocabularyResponse response) {
        String message = response.action == PersonalVocabularyResponse.Action.ADDED
                ? "Added \"" + response.term + "\" to your vocabulary."
                : "Updated your definition of \"" + response.term + "\".";
        viewModel.complete(new PersonalVocabularyViewModel.SaveResult(true, response.wordId,
                response.term, response.definition, message));
    }

    @Override
    public void presentFailure(String message) {
        viewModel.complete(new PersonalVocabularyViewModel.SaveResult(false, "", "", "", message));
    }

    public ViewModels.VocabularyProgress progress(LearningProgress progress) {
        Map<String, String> lastReviewed = new LinkedHashMap<>();
        progress.getLastReviewed().forEach((id, instant) -> lastReviewed.put(id, reviewTime.format(instant)));
        return new ViewModels.VocabularyProgress(progress.getReviewWordIds(), progress.getReviewCounts(), lastReviewed);
    }
}
