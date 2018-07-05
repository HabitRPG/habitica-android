package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.WorldState;

import io.reactivex.Flowable;

public interface ContentRepository extends BaseRepository {

    Flowable<ContentResult> retrieveContent();
    Flowable<ContentResult> retrieveContent(boolean forced);

    Flowable<WorldState> retrieveWorldState();
}
