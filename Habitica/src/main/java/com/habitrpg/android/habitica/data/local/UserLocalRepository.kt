package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.shared.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.shared.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.shared.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.RealmResults

enum class UserQuestStatus {
    NO_QUEST,
    QUEST_COLLECT,
    QUEST_BOSS,
    QUEST_UNKNOWN
}

interface UserLocalRepository : BaseLocalRepository {

    fun getTutorialSteps(): Flowable<RealmResults<TutorialStep>>

    fun getUser(userID: String): Flowable<User>

    fun saveUser(user: User)

    fun saveMessages(messages: List<ChatMessage>)

    fun getSkills(user: User): Flowable<RealmResults<Skill>>

    fun getSpecialItems(user: User): Flowable<RealmResults<Skill>>
    fun getAchievements(): Flowable<RealmResults<Achievement>>
    fun getQuestAchievements(userID: String): Flowable<RealmResults<QuestAchievement>>
    fun getUserQuestStatus(userID: String): Flowable<UserQuestStatus>
}
