package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.WorldState;

import rx.Observable;

public interface ContentRepository extends BaseRepository {

    Observable<ContentResult> retrieveContent();
    Observable<ContentResult> retrieveContent(boolean forced);

    Observable<WorldState> retrieveWorldState();
}
