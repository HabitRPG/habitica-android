package com.habitrpg.android.habitica.data;

import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import java.util.Collection;
import java.util.List;

import rx.Observable;

public interface TagRepository extends BaseRepository {

    Observable<Tag> createTag(Tag tag);

    Observable<Tag> updateTag(Tag tag);

    Observable<Void> deleteTag(String id);


    Observable<Tag> createTags(Collection<Tag> tags);
    Observable<Tag> updateTags(Collection<Tag> tags);
    Observable<List<Void>> deleteTags(Collection<String> tagIds);
}
