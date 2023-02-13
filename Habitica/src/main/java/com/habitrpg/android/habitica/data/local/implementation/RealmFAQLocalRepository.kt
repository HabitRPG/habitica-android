package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.FAQLocalRepository
import com.habitrpg.android.habitica.models.FAQArticle
import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class RealmFAQLocalRepository(realm: Realm) : RealmContentLocalRepository(realm), FAQLocalRepository {
    override fun getArticle(position: Int): Flow<FAQArticle> {
        return realm.where(FAQArticle::class.java)
            .equalTo("position", position)
            .findAll()
            .toFlow()
            .filter { it.isLoaded && it.count() > 0 }
            .map { it.firstOrNull() }
            .filterNotNull()
    }

    override val articles: Flow<List<FAQArticle>>
        get() = realm.where(FAQArticle::class.java)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
}
