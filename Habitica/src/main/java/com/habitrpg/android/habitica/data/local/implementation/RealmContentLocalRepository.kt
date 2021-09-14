package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.extensions.skipNull
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.social.Group
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm

open class RealmContentLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), ContentLocalRepository {

    override fun saveContent(contentResult: ContentResult) {
        executeTransactionAsync { realm1 ->
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
            realm1.insertOrUpdate(contentResult.appearances)
            realm1.insertOrUpdate(contentResult.backgrounds)
            realm1.insertOrUpdate(contentResult.faq)
        }
    }

    override fun getWorldState(): Flowable<WorldState> {
        return RxJavaBridge.toV3Flowable(
            realm.where(WorldState::class.java)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded && it.size > 0 }
                .map { it.first() }
        )
            .skipNull()
    }

    override fun saveWorldState(worldState: WorldState) {
        val tavern = getUnmanagedCopy(
            realm.where(Group::class.java)
                .equalTo("id", Group.TAVERN_ID)
                .findFirst() ?: Group()
        )
        if (!tavern.isManaged) {
            tavern.id = Group.TAVERN_ID
        }
        if (tavern.quest == null) {
            tavern.quest = Quest()
        }
        tavern.quest?.active = worldState.worldBossActive
        tavern.quest?.key = worldState.worldBossKey
        tavern.quest?.progress = worldState.progress
        save(tavern)
        save(worldState)
    }
}
