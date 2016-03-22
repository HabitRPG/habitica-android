package com.habitrpg.android.habitica;

import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.QuestBoss;
import com.magicmicky.habitrpgwrapper.lib.models.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ContentCache {
    public interface GotContentEntryCallback<T extends Object> {
        void GotObject(T obj);
    }

    public interface QuestContentCallback {
        void GotQuest(QuestContent content);
    }


    private ApiService apiService;

    public ContentCache(ApiService apiService) {

        this.apiService = apiService;
    }

    public void GetQuestContent(final String key, final QuestContentCallback cb) {
        final QuestContent quest = new Select().from(QuestContent.class).where(Condition.column("key").eq(key)).querySingle();

        if (quest != null) {
            quest.boss = new Select().from(QuestBoss.class).where(Condition.column("key").eq(key)).querySingle();
            cb.GotQuest(quest);
        } else {

            getContentAndSearchFor("quest", key, new GotContentEntryCallback<QuestContent>() {
                @Override
                public void GotObject(QuestContent obj) {
                    cb.GotQuest(obj);
                }
            });
        }
    }

    public void GetItemData(final String key, final GotContentEntryCallback<ItemData> gotEntry) {
        final ItemData itemData = new Select().from(ItemData.class).where(Condition.column("key").eq(key)).querySingle();

        if (itemData != null) {
            gotEntry.GotObject(itemData);
        } else {
            getContentAndSearchFor("item", key, gotEntry);
        }
    }

    public void GetItemDataList(final List<String> keysToSearch, GotContentEntryCallback<List<ItemData>> gotEntries) {

        Condition.In keyCondition = Condition.column("key").in("potion");

        for (String item : keysToSearch) {
            keyCondition = keyCondition.and(item);
        }


        Where<ItemData> query = new Select().from(ItemData.class).where(keyCondition);

        List<ItemData> items = query.queryList();

        if (items != null && items.size() == keysToSearch.size()) {
            gotEntries.GotObject(items);
        } else {
            getContentAndSearchForList("item", keysToSearch, gotEntries);
        }
    }

    private <T extends Object> void getContentAndSearchFor(final String typeOfSearch, final String searchKey, final GotContentEntryCallback<T> gotEntry) {
        apiService.getContent(new Callback<ContentResult>() {
            @Override
            public void success(ContentResult contentResult, Response response) {
                switch (typeOfSearch) {
                    case "quest": {
                        Collection<QuestContent> questList = contentResult.quests.values();

                        for (QuestContent quest : questList) {
                            if (quest.key == searchKey) {
                                gotEntry.GotObject((T) quest);
                            }
                        }

                        break;
                    }
                    case "item": {
                        T searchedItem = null;

                        if (searchKey == "potion") {
                            searchedItem = (T) contentResult.potion;
                        } else if (searchKey == "armoire") {
                            searchedItem = (T) contentResult.armoire;
                        } else {
                            Collection<ItemData> itemList = contentResult.gear.flat;

                            for (ItemData item : itemList) {
                                if (item.key.equals(searchKey)) {
                                    searchedItem = (T) item;
                                }
                            }
                        }

                        gotEntry.GotObject((T) searchedItem);

                        break;
                    }
                }

                saveContentResultToDb(contentResult);
            }


            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private <T extends Object> void getContentAndSearchForList(final String typeOfSearch, final List<String> searchKeys, final GotContentEntryCallback<List<T>> gotEntry) {
        apiService.getContent(new Callback<ContentResult>() {
            @Override
            public void success(ContentResult contentResult, Response response) {

                switch (typeOfSearch) {
                    case "item": {
                        List<T> resultList = new ArrayList<T>();

                        List<ItemData> itemList = new ArrayList<ItemData>(contentResult.gear.flat);
                        itemList.add(contentResult.potion);
                        itemList.add(contentResult.armoire);

                        for (ItemData item : itemList) {
                            if (searchKeys.contains(item.key)) {
                                resultList.add((T) item);
                            }
                        }

                        gotEntry.GotObject(resultList);

                        break;
                    }
                }

                saveContentResultToDb(contentResult);
            }


            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


    private void saveContentResultToDb(ContentResult contentResult) {
        Collection<QuestContent> questList = contentResult.quests.values();

        for (QuestContent quest : questList) {
            quest.save();

            if (quest.boss != null) {
                quest.boss.key = quest.key;
                quest.boss.async().save();
            }
        }

        Collection<ItemData> itemList = new ArrayList<>(contentResult.gear.flat);
        itemList.add(contentResult.armoire);
        itemList.add(contentResult.potion);

        for (ItemData item : itemList) {
            item.async().save();
        }
    }
}
