package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.RealmResults

interface UserLocalRepository : BaseLocalRepository {

    fun getTutorialSteps(): Flowable<RealmResults<TutorialStep>>

    fun getUser(userID: String): Flowable<User>

    fun saveUser(user: User)

    fun saveMessages(messages: List<ChatMessage>)

    fun getSkills(user: User): Flowable<RealmResults<Skill>>

    fun getSpecialItems(user: User): Flowable<RealmResults<Skill>>

    fun getInboxMessages(userId: String, replyToUserID: String?): Flowable<RealmResults<ChatMessage>>

    fun getInboxOverviewList(userId: String): Flowable<RealmResults<ChatMessage>>
}
