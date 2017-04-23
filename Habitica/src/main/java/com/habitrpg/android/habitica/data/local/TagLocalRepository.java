package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.Tag;

import java.util.List;

import rx.Observable;

public interface TagLocalRepository extends BaseLocalRepository {
    Observable<List<Tag>> getTags();

    void removeOldTags(List<Tag> onlineTags);
}
