package com.habitrpg.android.habitica.data.local.implementation;


import com.habitrpg.android.habitica.data.local.TagLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import rx.Observable;

public class DbFlowTagLocalRepository implements TagLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<List<Tag>> getTags() {
        return Observable.defer(() -> Observable.just(new Select().from(Tag.class)
                .orderBy(OrderBy.columns("position", "dateCreated").descending())
                .queryList()));
    }
}
