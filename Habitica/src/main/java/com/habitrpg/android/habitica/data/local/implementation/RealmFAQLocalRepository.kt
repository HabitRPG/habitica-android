package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.FAQLocalRepository
import com.habitrpg.android.habitica.models.FAQArticle
import io.realm.Realm
import kotlinx.coroutines.flow.Flow

class RealmFAQLocalRepository(
    realm: Realm,
) : RealmContentLocalRepository(realm),
    FAQLocalRepository {
    override fun getArticle(position: Int): Flow<FAQArticle> = safeFindOne {
        it.where(FAQArticle::class.java).equalTo("position", position)
    }

    override val articles: Flow<List<FAQArticle>>
        get() = safeFindAll { it.where(FAQArticle::class.java) }
}
