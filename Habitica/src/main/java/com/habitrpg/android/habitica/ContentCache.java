package com.habitrpg.android.habitica;

import com.magicmicky.habitrpgwrapper.lib.models.QuestBoss;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ContentCache {
    private APIHelper apiHelper;


    public ContentCache(APIHelper apiHelper) {
        this.apiHelper = apiHelper;
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

        Condition.In keyCondition = Condition.column("key").in("");

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

    private <T> void getContentAndSearchFor(final String typeOfSearch, final String searchKey, final GotContentEntryCallback<T> gotEntry) {
        apiHelper.getContent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contentResult -> {
                    switch (typeOfSearch) {
                        case "quest": {
                            Collection<QuestContent> questList = contentResult.quests;

                            for (QuestContent quest : questList) {
                                if (quest.getKey().equals(searchKey)) {
                                    gotEntry.GotObject((T) quest);
                                }
                            }

                            break;
                        }
                        case "item": {
                            T searchedItem = null;

                            if (searchKey.equals("potion")) {
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

                            gotEntry.GotObject(searchedItem);

                            break;
                        }
                    }
                }, throwable -> {
                });
    }

    private void getContentAndSearchForList(final String typeOfSearch, final List<String> searchKeys, final GotContentEntryCallback<List<ItemData>> gotEntry) {
        List<ItemData> resultList = new ArrayList<>();
        apiHelper.getContent()
                .flatMap(contentResult -> {
                    List<ItemData> itemList = new ArrayList<ItemData>(contentResult.gear.flat);
                    itemList.add(contentResult.potion);
                    itemList.add(contentResult.armoire);
                    return Observable.from(itemList);
                })
                .filter(item -> searchKeys.contains(item.key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultList::add, throwable -> {
                }, () -> gotEntry.GotObject(resultList));
    }

    public interface GotContentEntryCallback<T extends Object> {
        void GotObject(T obj);
    }

    public interface QuestContentCallback {
        void GotQuest(QuestContent content);
    }
}
