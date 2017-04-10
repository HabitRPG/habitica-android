package com.habitrpg.android.habitica;

import android.support.annotation.Nullable;

import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.QuestBoss;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import rx.Observable;


public class ContentCache {
    private static final String CONTENT_TYPE_ITEM = "item";
    private static final String CONTENT_TYPE_QUEST = "quest";
    private ApiClient apiClient;


    public ContentCache(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void getQuestContent(final String key, final QuestContentCallback cb) {
        final QuestContent quest = new Select().from(QuestContent.class).where(Condition.column(ItemData.UNIQUE_IDENTIFIER).eq(key)).querySingle();

        if (quest != null) {
            quest.boss = new Select().from(QuestBoss.class).where(Condition.column(ItemData.UNIQUE_IDENTIFIER).eq(key)).querySingle();
            cb.gotQuest(quest);
        } else {

            getContentAndSearchFor(CONTENT_TYPE_QUEST, key, cb::gotQuest);
        }
    }

    public void getItemData(final String key, final GotContentEntryCallback<ItemData> gotEntry) {
        final ItemData itemData = new Select().from(ItemData.class).where(Condition.column(ItemData.UNIQUE_IDENTIFIER).eq(key)).querySingle();

        if (itemData != null) {
            gotEntry.gotObject(itemData);
        } else {
            getContentAndSearchFor(CONTENT_TYPE_ITEM, key, gotEntry);
        }
    }

    public void getItemDataList(final List<String> keysToSearch, GotContentEntryCallback<List<ItemData>> gotEntries) {

        Condition.In keyCondition = Condition.column(ItemData.UNIQUE_IDENTIFIER).in("");

        for (String item : keysToSearch) {
            keyCondition = keyCondition.and(item);
        }

        Where<ItemData> query = new Select().from(ItemData.class).where(keyCondition);

        List<ItemData> items = query.queryList();

        if (items != null && items.size() == keysToSearch.size()) {
            gotEntries.gotObject(items);
        } else {
            getContentAndSearchForList(keysToSearch, gotEntries);
        }
    }

    private <T> void getContentAndSearchFor(final String typeOfSearch, final String searchKey, final GotContentEntryCallback<T> gotEntry) {
        apiClient.getContent()

                .subscribe(contentResult -> {
                    switch (typeOfSearch) {
                        case CONTENT_TYPE_QUEST: {
                            Collection<QuestContent> questList = contentResult.quests;

                            for (QuestContent quest : questList) {
                                if (quest.getKey().equals(searchKey)) {
                                    gotEntry.gotObject((T) quest);
                                }
                            }

                            break;
                        }
                        case CONTENT_TYPE_ITEM: {
                            T searchedItem = null;

                            if ("potion".equals(searchKey)) {
                                searchedItem = (T) contentResult.potion;
                            } else if ("armoire".equals(searchKey)) {
                                searchedItem = (T) contentResult.armoire;
                            } else {
                                Collection<ItemData> itemList = contentResult.gear.flat;

                                for (ItemData item : itemList) {
                                    if (item.key.equals(searchKey)) {
                                        searchedItem = (T) item;
                                    }
                                }
                            }

                            gotEntry.gotObject(searchedItem);

                            break;
                        }
                        default:
                            break;
                    }
                }, throwable -> {
                });
    }

    private void getContentAndSearchForList(final List<String> searchKeys, final GotContentEntryCallback<List<ItemData>> gotEntry) {
        List<ItemData> resultList = new ArrayList<>();
        apiClient.getContent()

                .flatMap(contentResult -> {
                    List<ItemData> itemList = new ArrayList<>(contentResult.gear.flat);
                    itemList.add(contentResult.potion);
                    itemList.add(contentResult.armoire);
                    return Observable.from(itemList);
                })
                .filter(item -> searchKeys.contains(item.key))
                .subscribe(resultList::add, throwable -> {
                }, () -> gotEntry.gotObject(resultList));
    }

    public interface GotContentEntryCallback<T extends Object> {
        void gotObject(@Nullable T obj);
    }

    public interface QuestContentCallback {
        void gotQuest(QuestContent content);
    }
}
