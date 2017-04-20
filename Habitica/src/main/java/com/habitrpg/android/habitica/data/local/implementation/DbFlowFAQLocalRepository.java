package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.FAQLocalRepository;
import com.habitrpg.android.habitica.models.FAQArticle;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import rx.Observable;

public class DbFlowFAQLocalRepository implements FAQLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<List<FAQArticle>> getArticles() {
        return Observable.defer(() -> Observable.just(new Select().from(FAQArticle.class).queryList()));
    }
}
