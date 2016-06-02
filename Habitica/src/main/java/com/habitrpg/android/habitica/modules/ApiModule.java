package com.habitrpg.android.habitica.modules;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.database.CheckListItemExcludeStrategy;
import com.magicmicky.habitrpgwrapper.lib.api.MaintenanceApiService;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.Purchases;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Mount;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.responses.FeedResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.magicmicky.habitrpgwrapper.lib.utils.BooleanAsIntAdapter;
import com.magicmicky.habitrpgwrapper.lib.utils.ChatMessageDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.ChecklistItemSerializer;
import com.magicmicky.habitrpgwrapper.lib.utils.ContentDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.CustomizationDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.DateDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.EggListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.FAQArticleListDeserilializer;
import com.magicmicky.habitrpgwrapper.lib.utils.FeedResponseDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.FoodListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.GroupSerialization;
import com.magicmicky.habitrpgwrapper.lib.utils.HatchingPotionListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.ItemDataListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.MountListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.PetListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.PurchasedDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.QuestListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.SkillDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskListDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskSerializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TaskTagDeserializer;
import com.magicmicky.habitrpgwrapper.lib.utils.TutorialStepListDeserializer;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApiModule {

    @Provides
    @Singleton
    public HostConfig providesHostConfig(SharedPreferences sharedPreferences, Context context) {
        return new HostConfig(sharedPreferences, context);
    }

    @Provides
    public GsonConverterFactory providesGsonConverterFactory() {
        Type taskTagClassListType = new TypeToken<List<TaskTag>>() {}.getType();
        Type skillListType = new TypeToken<List<Skill>>() {}.getType();
        Type customizationListType = new TypeToken<List<Customization>>() {}.getType();
        Type tutorialStepListType = new TypeToken<List<TutorialStep>>() {}.getType();
        Type faqArticleListType = new TypeToken<List<FAQArticle>>() {}.getType();
        Type itemDataListType = new TypeToken<List<ItemData>>() {}.getType();
        Type eggListType = new TypeToken<List<Egg>>() {}.getType();
        Type foodListType = new TypeToken<List<Food>>() {}.getType();
        Type hatchingPotionListType = new TypeToken<List<HatchingPotion>>() {}.getType();
        Type questContentListType = new TypeToken<List<QuestContent>>() {}.getType();
        Type petListType = new TypeToken<HashMap<String, Pet>>() {}.getType();
        Type mountListType = new TypeToken<HashMap<String, Mount>>() {}.getType();

        //Exclusion strategy needed for DBFlow https://github.com/Raizlabs/DBFlow/issues/121
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new CheckListItemExcludeStrategy())
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaredClass().equals(ModelAdapter.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(taskTagClassListType, new TaskTagDeserializer())
                .registerTypeAdapter(Boolean.class, new BooleanAsIntAdapter())
                .registerTypeAdapter(boolean.class, new BooleanAsIntAdapter())
                .registerTypeAdapter(skillListType, new SkillDeserializer())
                .registerTypeAdapter(ChecklistItem.class, new ChecklistItemSerializer())
                .registerTypeAdapter(TaskList.class, new TaskListDeserializer())
                .registerTypeAdapter(Purchases.class, new PurchasedDeserializer())
                .registerTypeAdapter(customizationListType, new CustomizationDeserializer())
                .registerTypeAdapter(tutorialStepListType, new TutorialStepListDeserializer())
                .registerTypeAdapter(faqArticleListType, new FAQArticleListDeserilializer())
                .registerTypeAdapter(Group.class, new GroupSerialization())
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapter(itemDataListType, new ItemDataListDeserializer())
                .registerTypeAdapter(eggListType, new EggListDeserializer())
                .registerTypeAdapter(foodListType, new FoodListDeserializer())
                .registerTypeAdapter(hatchingPotionListType, new HatchingPotionListDeserializer())
                .registerTypeAdapter(questContentListType, new QuestListDeserializer())
                .registerTypeAdapter(petListType, new PetListDeserializer())
                .registerTypeAdapter(mountListType, new MountListDeserializer())
                .registerTypeAdapter(ChatMessage.class, new ChatMessageDeserializer())
                .registerTypeAdapter(Task.class, new TaskSerializer())
                .registerTypeAdapter(ContentResult.class, new ContentDeserializer())
                .registerTypeAdapter(FeedResponse.class, new FeedResponseDeserializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @Singleton
    public APIHelper providesApiHelper(GsonConverterFactory gsonConverter, HostConfig hostConfig) {
        return new APIHelper(gsonConverter, hostConfig);
    }

    @Provides
    public MaintenanceApiService providesMaintenanceApiService(GsonConverterFactory gsonConverter, HostConfig hostConfig) {
        Retrofit adapter = new Retrofit.Builder()
                .baseUrl("https://habitica-assets.s3.amazonaws.com/mobileApp/endpoint/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .build();
        return adapter.create(MaintenanceApiService.class);
    }
}
