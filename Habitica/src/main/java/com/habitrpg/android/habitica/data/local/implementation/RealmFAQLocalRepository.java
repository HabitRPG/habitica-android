package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.FAQLocalRepository;
import com.habitrpg.android.habitica.models.FAQArticle;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;

public class RealmFAQLocalRepository extends RealmBaseLocalRepository implements FAQLocalRepository {

    public RealmFAQLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<RealmResults<FAQArticle>> getArticles() {
        return realm.where(FAQArticle.class)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }
}
