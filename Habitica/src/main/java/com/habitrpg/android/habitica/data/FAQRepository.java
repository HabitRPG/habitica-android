package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.FAQArticle;

import java.util.List;

import rx.Observable;

public interface FAQRepository extends BaseRepository {
    Observable<List<FAQArticle>> getArticles();

}
