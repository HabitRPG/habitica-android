package com.habitrpg.android.habitica.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.FAQArticle;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.WorldState;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.inventory.QuestCollect;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.inventory.QuestDropItem;
import com.habitrpg.android.habitica.models.responses.FeedResponse;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.user.Purchases;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.utils.BooleanAsIntAdapter;
import com.habitrpg.android.habitica.utils.ChallengeDeserializer;
import com.habitrpg.android.habitica.utils.ChallengeListDeserializer;
import com.habitrpg.android.habitica.utils.ChatMessageDeserializer;
import com.habitrpg.android.habitica.utils.ChatMessageListDeserializer;
import com.habitrpg.android.habitica.utils.ContentDeserializer;
import com.habitrpg.android.habitica.utils.CustomizationDeserializer;
import com.habitrpg.android.habitica.utils.DateDeserializer;
import com.habitrpg.android.habitica.utils.EggListDeserializer;
import com.habitrpg.android.habitica.utils.EquipmentListDeserializer;
import com.habitrpg.android.habitica.utils.FAQArticleListDeserilializer;
import com.habitrpg.android.habitica.utils.FeedResponseDeserializer;
import com.habitrpg.android.habitica.utils.FoodListDeserializer;
import com.habitrpg.android.habitica.utils.GroupSerialization;
import com.habitrpg.android.habitica.utils.HatchingPotionListDeserializer;
import com.habitrpg.android.habitica.utils.MemberSerialization;
import com.habitrpg.android.habitica.utils.MountListDeserializer;
import com.habitrpg.android.habitica.utils.MountMapDeserializer;
import com.habitrpg.android.habitica.utils.PetListDeserializer;
import com.habitrpg.android.habitica.utils.PetMapDeserializer;
import com.habitrpg.android.habitica.utils.PurchasedDeserializer;
import com.habitrpg.android.habitica.utils.QuestCollectDeserializer;
import com.habitrpg.android.habitica.utils.QuestDeserializer;
import com.habitrpg.android.habitica.utils.QuestDropItemsListSerialization;
import com.habitrpg.android.habitica.utils.QuestListDeserializer;
import com.habitrpg.android.habitica.utils.SkillDeserializer;
import com.habitrpg.android.habitica.utils.TaskListDeserializer;
import com.habitrpg.android.habitica.utils.TaskSerializer;
import com.habitrpg.android.habitica.utils.TaskTagDeserializer;
import com.habitrpg.android.habitica.utils.TutorialStepListDeserializer;
import com.habitrpg.android.habitica.utils.UserDeserializer;
import com.habitrpg.android.habitica.utils.WorldStateSerialization;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import retrofit2.converter.gson.GsonConverterFactory;

public class GSonFactoryCreator {

    public static GsonConverterFactory create() {
        Type taskTagClassListType = new TypeToken<RealmList<Tag>>() {}.getType();
        Type skillListType = new TypeToken<List<Skill>>() {}.getType();
        Type customizationListType = new TypeToken<RealmList<Customization>>() {}.getType();
        Type tutorialStepListType = new TypeToken<RealmList<TutorialStep>>() {}.getType();
        Type faqArticleListType = new TypeToken<RealmList<FAQArticle>>() {}.getType();
        Type itemDataListType = new TypeToken<RealmList<Equipment>>() {}.getType();
        Type eggListType = new TypeToken<RealmList<Egg>>() {}.getType();
        Type foodListType = new TypeToken<RealmList<Food>>() {}.getType();
        Type hatchingPotionListType = new TypeToken<RealmList<HatchingPotion>>() {}.getType();
        Type questContentListType = new TypeToken<RealmList<QuestContent>>() {}.getType();
        Type petMapType = new TypeToken<Map<String, Pet>>() {}.getType();
        Type mountMapType = new TypeToken<Map<String, Mount>>() {}.getType();
        Type petListType = new TypeToken<RealmList<Pet>>() {}.getType();
        Type mountListType = new TypeToken<RealmList<Mount>>() {}.getType();
        Type questCollectListType = new TypeToken<RealmList<QuestCollect>>() {}.getType();
        Type chatMessageListType = new TypeToken<RealmList<ChatMessage>>() {}.getType();
        Type challengeListType = new TypeToken<List<Challenge>>() {}.getType();
        Type challengeRealmListType = new TypeToken<RealmList<Challenge>>() {}.getType();
        Type questDropItemListType = new TypeToken<RealmList<QuestDropItem>>() {}.getType();

        //Exclusion strategy needed for DBFlow https://github.com/Raizlabs/DBFlow/issues/121
        Gson gson = new GsonBuilder()
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
                .registerTypeAdapter(eggListType, new EggListDeserializer())
                .registerTypeAdapter(foodListType, new FoodListDeserializer())
                .registerTypeAdapter(hatchingPotionListType, new HatchingPotionListDeserializer())
                .registerTypeAdapter(questContentListType, new QuestListDeserializer())
                .registerTypeAdapter(petListType, new PetListDeserializer())
                .registerTypeAdapter(mountListType, new MountListDeserializer())
                .registerTypeAdapter(petMapType, new PetMapDeserializer())
                .registerTypeAdapter(mountMapType, new MountMapDeserializer())
                .registerTypeAdapter(ChatMessage.class, new ChatMessageDeserializer())
                .registerTypeAdapter(Task.class, new TaskSerializer())
                .registerTypeAdapter(ContentResult.class, new ContentDeserializer())
                .registerTypeAdapter(FeedResponse.class, new FeedResponseDeserializer())
                .registerTypeAdapter(Challenge.class, new ChallengeDeserializer())
                .registerTypeAdapter(User.class, new UserDeserializer())
                .registerTypeAdapter(questCollectListType, new QuestCollectDeserializer())
                .registerTypeAdapter(chatMessageListType, new ChatMessageListDeserializer())
                .registerTypeAdapter(challengeListType, new ChallengeListDeserializer())
                .registerTypeAdapter(challengeRealmListType, new ChallengeListDeserializer())
                .registerTypeAdapter(questDropItemListType, new QuestDropItemsListSerialization())
                .registerTypeAdapter(Quest.class, new QuestDeserializer())
                .registerTypeAdapter(Member.class, new MemberSerialization())
                .registerTypeAdapter(WorldState.class, new WorldStateSerialization())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
        return GsonConverterFactory.create(gson);
    }
}
