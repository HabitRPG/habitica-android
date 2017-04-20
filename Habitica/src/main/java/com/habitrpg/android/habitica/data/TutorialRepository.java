package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import rx.Observable;

public interface TutorialRepository extends BaseRepository {

    Observable<TutorialStep> getTutorialStep(String key);
    Observable<List<TutorialStep>> getTutorialSteps(List<String> keys);

}
