package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User

import io.realm.RealmResults
import rx.Observable

interface UserLocalRepository : BaseLocalRepository {

    val tutorialSteps: Observable<RealmResults<TutorialStep>>

    fun getUser(userID: String): Observable<User>

    fun saveUser(user: User)

    fun getSkills(user: User): Observable<RealmResults<Skill>>

    fun getSpecialItems(user: User): Observable<RealmResults<Skill>>

    fun getInboxMessages(userId: String, replyToUserID: String): Observable<RealmResults<ChatMessage>>

    fun getInboxOverviewList(userId: String): Observable<RealmResults<ChatMessage>>
}
