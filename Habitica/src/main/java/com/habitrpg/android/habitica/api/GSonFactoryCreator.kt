package com.habitrpg.android.habitica.api

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.habitrpg.android.habitica.models.*
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.FeedResponse
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.*
import com.habitrpg.android.habitica.utils.*
import io.realm.RealmList
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

object GSonFactoryCreator {
    fun create(): GsonConverterFactory {
        val skillListType = object : TypeToken<List<Skill?>?>() {}.type
        val taskTagClassListType = object : TypeToken<RealmList<Tag?>?>() {}.type
        val customizationListType = object : TypeToken<RealmList<Customization?>?>() {}.type
        val tutorialStepListType = object : TypeToken<RealmList<TutorialStep?>?>() {}.type
        val faqArticleListType = object : TypeToken<RealmList<FAQArticle?>?>() {}.type
        val itemDataListType = object : TypeToken<RealmList<Equipment?>?>() {}.type
        val questCollectListType = object : TypeToken<RealmList<QuestCollect?>?>() {}.type
        val chatMessageListType = object : TypeToken<RealmList<ChatMessage?>?>() {}.type
        val challengeListType = object : TypeToken<List<Challenge?>?>() {}.type
        val challengeRealmListType = object : TypeToken<RealmList<Challenge?>?>() {}.type
        val questDropItemListType = object : TypeToken<RealmList<QuestDropItem?>?>() {}.type
        val ownedItemListType = object : TypeToken<RealmList<OwnedItem?>?>() {}.type
        val ownedPetListType = object : TypeToken<RealmList<OwnedPet?>?>() {}.type
        val ownedMountListType = object : TypeToken<RealmList<OwnedMount?>?>() {}.type
        val achievementsListType = object : TypeToken<List<Achievement?>?>() {}.type

        val gson = GsonBuilder()
                .registerTypeAdapter(taskTagClassListType, TaskTagDeserializer())
                .registerTypeAdapter(Boolean::class.java, BooleanAsIntAdapter())
                .registerTypeAdapter(Boolean::class.javaPrimitiveType, BooleanAsIntAdapter())
                .registerTypeAdapter(skillListType, SkillDeserializer())
                .registerTypeAdapter(TaskList::class.java, TaskListDeserializer())
                .registerTypeAdapter(Task::class.java, TaskSerializer())
                .registerTypeAdapter(Purchases::class.java, PurchasedDeserializer())
                .registerTypeAdapter(customizationListType, CustomizationDeserializer())
                .registerTypeAdapter(tutorialStepListType, TutorialStepListDeserializer())
                .registerTypeAdapter(faqArticleListType, FAQArticleListDeserilializer())
                .registerTypeAdapter(Group::class.java, GroupSerialization())
                .registerTypeAdapter(Date::class.java, DateDeserializer())
                .registerTypeAdapter(itemDataListType, EquipmentListDeserializer())
                .registerTypeAdapter(ChatMessage::class.java, ChatMessageDeserializer())
                .registerTypeAdapter(Task::class.java, TaskSerializer())
                .registerTypeAdapter(ContentResult::class.java, ContentDeserializer())
                .registerTypeAdapter(FeedResponse::class.java, FeedResponseDeserializer())
                .registerTypeAdapter(Challenge::class.java, ChallengeDeserializer())
                .registerTypeAdapter(User::class.java, UserDeserializer())
                .registerTypeAdapter(questCollectListType, QuestCollectDeserializer())
                .registerTypeAdapter(chatMessageListType, ChatMessageListDeserializer())
                .registerTypeAdapter(challengeListType, ChallengeListDeserializer())
                .registerTypeAdapter(challengeRealmListType, ChallengeListDeserializer())
                .registerTypeAdapter(questDropItemListType, QuestDropItemsListSerialization())
                .registerTypeAdapter(ownedItemListType, OwnedItemListDeserializer())
                .registerTypeAdapter(ownedPetListType, OwnedPetListDeserializer())
                .registerTypeAdapter(ownedMountListType, OwnedMountListDeserializer())
                .registerTypeAdapter(achievementsListType, AchievementListDeserializer())
                .registerTypeAdapter(Quest::class.java, QuestDeserializer())
                .registerTypeAdapter(Member::class.java, MemberSerialization())
                .registerTypeAdapter(WorldState::class.java, WorldStateSerialization())
                .registerTypeAdapter(FindUsernameResult::class.java, FindUsernameResultDeserializer())
                .registerTypeAdapter(Notification::class.java, NotificationDeserializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create()
        return GsonConverterFactory.create(gson)
    }
}