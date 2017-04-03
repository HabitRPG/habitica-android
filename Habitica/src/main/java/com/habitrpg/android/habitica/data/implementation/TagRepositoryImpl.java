package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.TagRepository;
import com.habitrpg.android.habitica.data.local.TagLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import rx.Observable;


public class TagRepositoryImpl extends BaseRepositoryImpl<TagLocalRepository> implements TagRepository {

    public TagRepositoryImpl(TagLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<Tag> createTag(Tag tag) {
        return apiClient.createTag(tag);
    }

    @Override
    public Observable<Tag> updateTag(Tag tag) {
        return apiClient.updateTag(tag.id, tag);
    }

    @Override
    public Observable<Void> deleteTag(String id) {
        return apiClient.deleteTag(id);
    }
}
