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
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
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
import com.magicmicky.habitrpgwrapper.lib.utils.RemindersItemSerializer;
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
        return APIHelper.createGsonFactory();
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
