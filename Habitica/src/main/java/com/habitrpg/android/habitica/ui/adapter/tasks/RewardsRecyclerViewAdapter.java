package com.habitrpg.android.habitica.ui.adapter.tasks;


import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.inventory.ItemData;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class RewardsRecyclerViewAdapter extends BaseTasksRecyclerViewAdapter<RewardViewHolder> {

    private final InventoryRepository inventoryRepository;
    @Nullable
    private final HabitRPGUser user;

    public RewardsRecyclerViewAdapter(String taskType, TaskFilterHelper taskFilterHelper, int layoutResource, Context newContext, @Nullable HabitRPGUser user, InventoryRepository inventoryRepository) {
        super(taskType, taskFilterHelper, layoutResource, newContext, user != null ? user.getId() : null);
        this.user = user;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public RewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RewardViewHolder(getContentView(parent));
    }

    private void loadEquipmentRewards() {
        if (inventoryRepository != null) {
            inventoryRepository.getInventoryBuyableGear()
                    .flatMap(items -> {
                        // get itemdata list
                        ArrayList<String> itemKeys = new ArrayList<>();
                        for (ItemData item : items) {
                            itemKeys.add(item.key);
                        }
                        itemKeys.add("potion");
                        if (user != null && user.getFlags().getArmoireEnabled()) {
                            itemKeys.add("armoire");
                        }
                        return Observable.create((Observable.OnSubscribe<List<Task>>) subscriber -> inventoryRepository.getItems(itemKeys).subscribe(obj -> {
                            ArrayList<Task> buyableItems = new ArrayList<>();
                            if (obj != null) {
                                for (ItemData item : obj) {
                                    Task reward = new Task();
                                    reward.text = item.text;
                                    reward.notes = item.notes;
                                    reward.value = item.value;
                                    reward.setType("reward");
                                    reward.specialTag = "item";
                                    reward.setId(item.key);

                                    if ("armoire".equals(item.key)) {
                                        if (user != null && user.getFlags().getArmoireEmpty()) {
                                            reward.notes = context.getResources().getString(R.string.armoireNotesEmpty);
                                        } else {
                                            long gearCount = inventoryRepository.getArmoireRemainingCount();
                                            reward.notes = context.getResources().getString(R.string.armoireNotesFull, gearCount);
                                        }
                                    }

                                    buyableItems.add(reward);
                                }
                            }
                            subscriber.onNext(buyableItems);
                            subscriber.onCompleted();
                        }, throwable -> {}));
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
