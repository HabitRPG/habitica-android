package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.FAQArticle;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface FAQLocalRepository extends BaseLocalRepository {

    Observable<RealmResults<FAQArticle>> getArticles();
}
