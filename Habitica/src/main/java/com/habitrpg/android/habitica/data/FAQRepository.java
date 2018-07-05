package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.FAQArticle;

import io.reactivex.Flowable;
import io.realm.RealmResults;

public interface FAQRepository extends BaseRepository {
    Flowable<RealmResults<FAQArticle>> getArticles();

}
