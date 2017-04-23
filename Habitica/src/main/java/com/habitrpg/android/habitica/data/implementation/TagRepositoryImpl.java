package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.TagRepository;
import com.habitrpg.android.habitica.data.local.TagLocalRepository;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.models.Tag;

import java.util.Collection;
import java.util.List;

import rx.Observable;


public class TagRepositoryImpl extends BaseRepositoryImpl<TagLocalRepository> implements TagRepository {

    public TagRepositoryImpl(TagLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<List<Tag>> getTags() {
        return localRepository.getTags();
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

    @Override
    public Observable<Tag> createTags(Collection<Tag> tags) {
        return Observable.defer(() -> Observable.from(tags))
                .filter(tag -> !tag.getName().isEmpty())
                .flatMap(this::createTag);
    }

    @Override
    public Observable<Tag> updateTags(Collection<Tag> tags) {
        return Observable.defer(() -> Observable.from(tags))
                .flatMap(this::updateTag);
    }

    @Override
    public Observable<List<Void>> deleteTags(Collection<String> tagIds) {
        return Observable.defer(() -> Observable.from(tagIds))
                .flatMap(this::deleteTag)
                .toList();
    }

    @Override
    public void removeOldTags(List<Tag> onlineTags) {
        localRepository.removeOldTags(onlineTags);
    }
}
