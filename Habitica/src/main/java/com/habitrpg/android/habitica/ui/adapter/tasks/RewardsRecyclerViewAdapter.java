package com.habitrpg.android.habitica.ui.adapter.tasks;


import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;
import rx.Observable;

public class RewardsRecyclerViewAdapter extends RealmBaseTasksRecyclerViewAdapter<RewardViewHolder> {

    private final Context context;
    private InventoryRepository inventoryRepository;
    @Nullable
    private User user;

    public RewardsRecyclerViewAdapter(@Nullable OrderedRealmCollection<Task> data, boolean autoUpdate, int layoutResource, Context context) {
        super(data, autoUpdate, layoutResource, null);
        this.context = context;
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
                        for (Equipment item : items) {
                            itemKeys.add(item.key);
                        }
                        itemKeys.add("potion");
                        if (user != null && user.getFlags().getArmoireEnabled()) {
                            itemKeys.add("armoire");
                        }
                        return Observable.create((Observable.OnSubscribe<List<Task>>) subscriber -> inventoryRepository.getItems(itemKeys).subscribe(obj -> {
                            ArrayList<Task> buyableItems = new ArrayList<>();
                            if (obj != null) {
                                for (Equipment item : obj) {
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
                        notifyDataSetChanged();
                    }, throwable -> {
                    });
        }
    }
}
