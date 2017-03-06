package com.habitrpg.android.habitica.ui.adapter.tasks;


import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder;
import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import android.content.Context;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class RewardsRecyclerViewAdapter extends BaseTasksRecyclerViewAdapter<RewardViewHolder> {

    private final ContentCache contentCache;
    private final HabitRPGUser user;
    private ApiClient apiClient;

    public RewardsRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource, Context newContext, HabitRPGUser user, ApiClient apiClient) {
        super(taskType, tagsHelper, layoutResource, newContext, user.getId());
        this.user = user;
        this.apiClient = apiClient;
        this.contentCache = new ContentCache(apiClient);
    }

    @Override
    public RewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RewardViewHolder(getContentView(parent));
    }

    public void loadEquipmentRewards() {
        if (apiClient != null) {
            apiClient.getInventoryBuyableGear()

                    .flatMap(items -> {
                        // get itemdata list
                        ArrayList<String> itemKeys = new ArrayList<>();
                        for (ItemData item : items) {
                            itemKeys.add(item.key);
                        }
                        itemKeys.add("potion");
                        if (user.getFlags().getArmoireEnabled()) {
                            itemKeys.add("armoire");
                        }
                        return Observable.create((Observable.OnSubscribe<List<Task>>) subscriber -> {
                            contentCache.GetItemDataList(itemKeys, obj -> {
                                ArrayList<Task> buyableItems = new ArrayList<>();
                                for (ItemData item : obj) {
                                    Task reward = new Task();
                                    reward.text = item.text;
                                    reward.notes = item.notes;
                                    reward.value = item.value;
                                    reward.setType("reward");
                                    reward.specialTag = "item";
                                    reward.setId(item.key);

                                    if (item.key.equals("armoire")) {
                                        if (user.getFlags().getArmoireEmpty()) {
                                            reward.notes = context.getResources().getString(R.string.armoireNotesEmpty);
                                        } else {
                                            long gearCount = new Select().count()
                                                    .from(ItemData.class)
                                                    .where(Condition.CombinedCondition.begin(Condition.column("klass").eq("armoire"))
                                                            .and(Condition.column("owned").isNull())
                                                    ).count();
                                            reward.notes = context.getResources().getString(R.string.armoireNotesFull, gearCount);
                                        }
                                    }

                                    buyableItems.add(reward);
                                }
                                subscriber.onNext(buyableItems);
                                subscriber.onCompleted();
                            });
                        });
                    })
                    .subscribe(items -> {
                        this.filteredContent.addAll(items);
                        notifyDataSetChanged();
                    }, throwable -> {
                    });
        }
    }

    @Override
    protected void injectThis(AppComponent component) {
        HabiticaBaseApplication.getComponent().inject(this);
    }

    @Override
    public void setTasks(List<Task> tasks) {
        super.setTasks(tasks);
        this.loadEquipmentRewards();
    }
}
