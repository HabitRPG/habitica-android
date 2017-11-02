package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import rx.Observable

class RealmUserLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), UserLocalRepository {

    override val tutorialSteps: Observable<RealmResults<TutorialStep>>
        get() = realm.where(TutorialStep::class.java).findAll().asObservable()
                .filter({ it.isLoaded })

    override fun getUser(userID: String): Observable<User> {
        return realm.where(User::class.java)
                .equalTo("id", userID)
                .findAll()
                .asObservable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
                .map { users -> users.first() }
    }

    override fun saveUser(user: User) {
        realm.executeTransaction { realm1 -> realm1.insertOrUpdate(user) }
        if (user.tags != null) {
            removeOldTags(user.id, user.tags)
        }
    }

    private fun removeOldTags(userId: String, onlineTags: List<Tag>) {
        val tags = realm.where(Tag::class.java).equalTo("userId", userId).findAll().createSnapshot()
        val tagsToDelete = tags.filterNot { onlineTags.contains(it) }
        realm.executeTransaction {
            for (tag in tagsToDelete) {
                tag.deleteFromRealm()
            }
        }
    }

    override fun getSkills(user: User): Observable<RealmResults<Skill>> {
        return realm.where(Skill::class.java)
                .equalTo("habitClass", user.stats.getHabitClass())
                .lessThanOrEqualTo("lvl", user.stats.lvl)
                .findAll()
                .asObservable()
                .filter({ it.isLoaded })
    }

    override fun getSpecialItems(user: User): Observable<RealmResults<Skill>> {
        val specialItems = user.items.special
        val ownedItems = ArrayList<String>()
        if (specialItems != null) {
            if (specialItems.snowball > 0) {
                ownedItems.add("snowball")
            }
            if (specialItems.shinySeed > 0) {
                ownedItems.add("shinySeed")
            }
            if (specialItems.seafoam > 0) {
                ownedItems.add("seafoam")
            }
            if (specialItems.spookySparkles > 0) {
                ownedItems.add("spookySparkles")
            }
        }
        if (ownedItems.size == 0) {
            ownedItems.add("")
        }
        return realm.where(Skill::class.java)
                .`in`("key", ownedItems.toTypedArray())
                .findAll()
                .asObservable()
                .filter({ it.isLoaded })
    }

    override fun getInboxMessages(userId: String, replyToUserID: String?): Observable<RealmResults<ChatMessage>> {
        return realm.where(ChatMessage::class.java)
                .equalTo("isInboxMessage", true)
                .equalTo("uuid", replyToUserID)
                .findAllSorted("timestamp", Sort.DESCENDING)
                .asObservable()
                .filter({ it.isLoaded })
    }

    override fun getInboxOverviewList(userId: String): Observable<RealmResults<ChatMessage>> {
        return realm.where(ChatMessage::class.java)
                .equalTo("isInboxMessage", true)
                .distinct("uuid")
                .sort("timestamp", Sort.DESCENDING)
                .asObservable()
                .filter({ it.isLoaded })
    }
}
