package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.ContentResult;

import rx.Observable;

public interface ContentRepository extends BaseRepository {

    Observable<ContentResult> retrieveContent();
    Observable<ContentResult> retrieveContent(boolean forced);
}
