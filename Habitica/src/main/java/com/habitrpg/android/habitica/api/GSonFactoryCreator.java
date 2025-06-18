package com.habitrpg.android.habitica.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.habitrpg.android.habitica.models.Achievement;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.FAQArticle;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.WorldState;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.inventory.QuestCollect;
import com.habitrpg.android.habitica.models.inventory.QuestDropItem;
import com.habitrpg.android.habitica.models.invitations.InviteResponse;
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.FindUsernameResult;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.GroupAssignedDetails;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.user.OwnedItem;
import com.habitrpg.android.habitica.models.user.OwnedMount;
import com.habitrpg.android.habitica.models.user.OwnedPet;
import com.habitrpg.android.habitica.models.user.Purchases;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.auth.SocialAuthentication;
import com.habitrpg.android.habitica.utils.AchievementListDeserializer;
import com.habitrpg.android.habitica.utils.AssignedDetailsDeserializer;
import com.habitrpg.android.habitica.utils.BooleanAsIntAdapter;
import com.habitrpg.android.habitica.utils.ChallengeDeserializer;
import com.habitrpg.android.habitica.utils.ChallengeListDeserializer;
import com.habitrpg.android.habitica.utils.ChatMessageDeserializer;
import com.habitrpg.android.habitica.utils.ContentDeserializer;
import com.habitrpg.android.habitica.utils.CustomizationDeserializer;
import com.habitrpg.android.habitica.utils.DateDeserializer;
import com.habitrpg.android.habitica.utils.EquipmentListDeserializer;
import com.habitrpg.android.habitica.utils.FAQArticleListDeserilializer;
import com.habitrpg.android.habitica.utils.FeedResponseDeserializer;
import com.habitrpg.android.habitica.utils.FindUsernameResultDeserializer;
import com.habitrpg.android.habitica.utils.GroupSerialization;
import com.habitrpg.android.habitica.utils.InviteResponseDeserializer;
import com.habitrpg.android.habitica.utils.MemberSerialization;
import com.habitrpg.android.habitica.utils.NotificationDeserializer;
import com.habitrpg.android.habitica.utils.OwnedItemListDeserializer;
import com.habitrpg.android.habitica.utils.OwnedMountListDeserializer;
import com.habitrpg.android.habitica.utils.OwnedPetListDeserializer;
import com.habitrpg.android.habitica.utils.PurchasedDeserializer;
import com.habitrpg.android.habitica.utils.QuestCollectDeserializer;
import com.habitrpg.android.habitica.utils.QuestDeserializer;
import com.habitrpg.android.habitica.utils.QuestDropItemsListSerialization;
import com.habitrpg.android.habitica.utils.SkillDeserializer;
import com.habitrpg.android.habitica.utils.SocialAuthenticationDeserializer;
import com.habitrpg.android.habitica.utils.TaskListDeserializer;
import com.habitrpg.android.habitica.utils.TaskSerializer;
import com.habitrpg.android.habitica.utils.TaskTagDeserializer;
import com.habitrpg.android.habitica.utils.TutorialStepListDeserializer;
import com.habitrpg.android.habitica.utils.UserDeserializer;
import com.habitrpg.android.habitica.utils.WorldStateSerialization;
import com.habitrpg.common.habitica.models.Notification;
import com.habitrpg.shared.habitica.models.responses.FeedResponse;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import retrofit2.converter.gson.GsonConverterFactory;

public class GSonFactoryCreator {

    public static Gson createGson() {
        Type skillListType = new TypeToken<List<Skill>>() {
        }.getType();
        Type taskTagClassListType = new TypeToken<RealmList<Tag>>() {
        }.getType();
        Type customizationListType = new TypeToken<RealmList<Customization>>() {
        }.getType();
        Type tutorialStepListType = new TypeToken<RealmList<TutorialStep>>() {
        }.getType();
        Type faqArticleListType = new TypeToken<RealmList<FAQArticle>>() {
        }.getType();
        Type itemDataListType = new TypeToken<RealmList<Equipment>>() {
        }.getType();
        Type questCollectListType = new TypeToken<RealmList<QuestCollect>>() {
        }.getType();
        Type chatMessageListType = new TypeToken<RealmList<ChatMessage>>() {
        }.getType();
        Type challengeListType = new TypeToken<List<Challenge>>() {
        }.getType();
        Type challengeRealmListType = new TypeToken<RealmList<Challenge>>() {
        }.getType();
        Type questDropItemListType = new TypeToken<RealmList<QuestDropItem>>() {
        }.getType();
        Type ownedItemListType = new TypeToken<RealmList<OwnedItem>>() {
        }.getType();
        Type ownedPetListType = new TypeToken<RealmList<OwnedPet>>() {
        }.getType();
        Type ownedMountListType = new TypeToken<RealmList<OwnedMount>>() {
        }.getType();
        Type achievementsListType = new TypeToken<List<Achievement>>() {
        }.getType();
        Type assignedDetailsListType = new TypeToken<RealmList<GroupAssignedDetails>>() {
        }.getType();

        return new GsonBuilder()
                .registerTypeAdapter(taskTagClassListType, new TaskTagDeserializer())
                .registerTypeAdapter(Boolean.class, new BooleanAsIntAdapter())
                .registerTypeAdapter(boolean.class, new BooleanAsIntAdapter())
                .registerTypeAdapter(skillListType, new SkillDeserializer())
                .registerTypeAdapter(TaskList.class, new TaskListDeserializer())
                .registerTypeAdapter(Purchases.class, new PurchasedDeserializer())
                .registerTypeAdapter(customizationListType, new CustomizationDeserializer())
                .registerTypeAdapter(tutorialStepListType, new TutorialStepListDeserializer())
                .registerTypeAdapter(faqArticleListType, new FAQArticleListDeserilializer())
                .registerTypeAdapter(Group.class, new GroupSerialization())
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapter(itemDataListType, new EquipmentListDeserializer())
                .registerTypeAdapter(ChatMessage.class, new ChatMessageDeserializer())
                .registerTypeAdapter(Task.class, new TaskSerializer())
                .registerTypeAdapter(ContentResult.class, new ContentDeserializer())
                .registerTypeAdapter(FeedResponse.class, new FeedResponseDeserializer())
                .registerTypeAdapter(Challenge.class, new ChallengeDeserializer())
                .registerTypeAdapter(User.class, new UserDeserializer())
                .registerTypeAdapter(questCollectListType, new QuestCollectDeserializer())
                .registerTypeAdapter(challengeListType, new ChallengeListDeserializer())
                .registerTypeAdapter(challengeRealmListType, new ChallengeListDeserializer())
                .registerTypeAdapter(questDropItemListType, new QuestDropItemsListSerialization())
                .registerTypeAdapter(ownedItemListType, new OwnedItemListDeserializer())
                .registerTypeAdapter(ownedPetListType, new OwnedPetListDeserializer())
                .registerTypeAdapter(ownedMountListType, new OwnedMountListDeserializer())
                .registerTypeAdapter(achievementsListType, new AchievementListDeserializer())
                .registerTypeAdapter(assignedDetailsListType, new AssignedDetailsDeserializer())
                .registerTypeAdapter(Quest.class, new QuestDeserializer())
                .registerTypeAdapter(Member.class, new MemberSerialization())
                .registerTypeAdapter(InviteResponse.class, new InviteResponseDeserializer())
                .registerTypeAdapter(WorldState.class, new WorldStateSerialization())
                .registerTypeAdapter(FindUsernameResult.class, new FindUsernameResultDeserializer())
                .registerTypeAdapter(Notification.class, new NotificationDeserializer())
                .registerTypeAdapter(SocialAuthentication.class, new SocialAuthenticationDeserializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .serializeNulls()
                .setLenient()
                .create();
    }

    public static GsonConverterFactory create() {
        return GsonConverterFactory.create(createGson());
    }
}
