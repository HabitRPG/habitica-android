package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

open class RealmContentLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), ContentLocalRepository {

    override fun saveContent(contentResult: ContentResult) {
        executeTransaction { realm1 ->
            contentResult.potion?.let { realm1.insertOrUpdate(it) }
            contentResult.armoire?.let { realm1.insertOrUpdate(it) }
            contentResult.gear?.flat?.let { realm1.insertOrUpdate(it) }

            realm1.insertOrUpdate(contentResult.quests)
            realm1.insertOrUpdate(contentResult.eggs)
            realm1.insertOrUpdate(contentResult.food)
            realm1.insertOrUpdate(contentResult.hatchingPotions)
            realm1.insertOrUpdate(contentResult.special)

            realm1.insertOrUpdate(contentResult.pets)
            realm1.insertOrUpdate(contentResult.mounts)

            realm1.insertOrUpdate(contentResult.spells)
            realm1.insertOrUpdate(contentResult.special)
            realm1.insertOrUpdate(contentResult.appearances)
            realm1.insertOrUpdate(contentResult.backgrounds)
            realm1.insertOrUpdate(contentResult.faq)
        }
    }

    override fun getWorldState(): Flow<WorldState> {
        return realm.where(WorldState::class.java)
            .findAll()
            .toFlow()
            .filter { it.isLoaded && it.size > 0 }
            .map { it.first() }
            .filterNotNull()
    }

    override fun saveWorldState(worldState: WorldState) {
        save(worldState)
    }
}
