package com.habitrpg.android.habitica;

import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.QuestBoss;
import com.magicmicky.habitrpgwrapper.lib.models.QuestContent;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Negue on 29.09.2015.
 */
public class ContentCache {
    public interface QuestContentCallback{
        void GotQuest(QuestContent content);
    }


    private ApiService apiService;

    public ContentCache(ApiService apiService){

        this.apiService = apiService;
    }

    public void GetQuestContent(final String key, final QuestContentCallback cb){
        final QuestContent quest = new Select().from(QuestContent.class).where(Condition.column("key").eq(key)).querySingle();

        if(quest != null){
            QuestBoss boss = new Select().from(QuestBoss.class).where(Condition.column("key").eq(key)).querySingle();
            quest.boss = boss;

            cb.GotQuest(quest);
        }
        else
        {
            // load from api and save to db

            apiService.getContent(new Callback<ContentResult>() {
                @Override
                public void success(ContentResult contentResult, Response response) {

                    QuestContent searchedQuest = null;

                    for (QuestContent quest : contentResult.quests.values()) {
                        quest.save();

                        if(quest.boss != null) {
                            quest.boss.key = quest.key;
                            quest.boss.save();
                        }

                        if(quest.key.equals(key)){
                            searchedQuest = quest;
                        }
                    }

                    cb.GotQuest(searchedQuest);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }
}
