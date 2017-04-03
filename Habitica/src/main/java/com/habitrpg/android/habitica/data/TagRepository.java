package com.habitrpg.android.habitica.data;

import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import rx.Observable;

public interface TagRepository extends BaseRepository {

    Observable<Tag> createTag(Tag tag);

    Observable<Tag> updateTag(Tag tag);

    Observable<Void> deleteTag(String id);
}
