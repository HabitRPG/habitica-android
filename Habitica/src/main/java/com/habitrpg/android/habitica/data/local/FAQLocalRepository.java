package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.FAQArticle;

import java.util.List;

import rx.Observable;

public interface FAQLocalRepository extends BaseLocalRepository {

    Observable<List<FAQArticle>> getArticles();
}
