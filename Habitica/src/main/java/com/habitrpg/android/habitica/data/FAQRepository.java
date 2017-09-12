package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.FAQArticle;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface FAQRepository extends BaseRepository {
    Observable<RealmResults<FAQArticle>> getArticles();

}
