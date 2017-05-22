package com.habitrpg.android.habitica.ui.adapter.tasks;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;

public class RewardsRecyclerViewAdapter extends RecyclerView.Adapter<RewardViewHolder> implements TaskRecyclerViewAdapter {

    private final Context context;
    private OrderedRealmCollection<Task> data;
    private List<Task> equipmentRewards;
    private final int layoutResource;
    private InventoryRepository inventoryRepository;
    @Nullable
    private User user;

    public RewardsRecyclerViewAdapter(@Nullable OrderedRealmCollection<Task> data, Context context, int layoutResource, InventoryRepository inventoryRepository, @Nullable User user) {
        this.context = context;
        this.layoutResource = layoutResource;
        this.data = data;
        this.inventoryRepository = inventoryRepository;
        this.user = user;
    }

        private View getContentView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
    }

    @Override
    public RewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RewardViewHolder(getContentView(parent));
    }

    @Override
    public void onBindViewHolder(RewardViewHolder holder, int position) {
        if (data != null && position < data.size()) {
            holder.bindHolder(data.get(position), position);
        } else if (equipmentRewards != null) {
            holder.bindHolder(equipmentRewards.get(position-getCustomRewardCount()), position);
        }
    }

    @Override
    public int getItemCount() {
        int rewardCount = getCustomRewardCount();
        rewardCount += getEquipmentRewardCount();
        return rewardCount;
    }

    private int getEquipmentRewardCount() {
        return equipmentRewards != null ? equipmentRewards.size() : 0;
    }

    private int getCustomRewardCount() {
        return data != null ? data.size() : 0;
    }

    private void loadEquipmentRewards() {
        if (inventoryRepository != null) {
            inventoryRepository.getInventoryBuyableGear()
                    .map(items -> {
                        ArrayList<String> itemKeys = new ArrayList<>();
                        for (Equipment item : items) {
                            itemKeys.add(item.key);
                        }
                        itemKeys.add("potion");
                        if (user != null && user.getFlags() != null && user.getFlags().getArmoireEnabled()) {
                            itemKeys.add("armoire");
                        }
                        return itemKeys;
                    }).flatMap(itemKeys -> inventoryRepository.getItems(itemKeys))
                    .map(items -> {
                        ArrayList<Task> buyableItems = new ArrayList<>();
                        if (items != null) {
                            for (Equipment item : items) {
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
                        return buyableItems;
                    })
                    .subscribe(items -> {
                        equipmentRewards = items;
                        notifyDataSetChanged();
                    }, RxErrorHandler.handleEmptyError());
        }
    }

    @Override
    public void updateData(OrderedRealmCollection<Task> data) {
        this.data = data;
        loadEquipmentRewards();
    }

    @Override
    public void filter() {
    }
}
