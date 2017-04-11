package com.habitrpg.android.habitica.data.local;

import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import java.util.List;

import rx.Observable;

public interface TagLocalRepository extends BaseLocalRepository {
    Observable<List<Tag>> getTags();

    void removeOldTags(List<Tag> onlineTags);
}
