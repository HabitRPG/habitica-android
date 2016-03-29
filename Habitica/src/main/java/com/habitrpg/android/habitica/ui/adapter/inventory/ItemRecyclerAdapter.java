package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ItemItemBinding;
import com.habitrpg.android.habitica.events.commands.InvitePartyToQuestCommand;
import com.habitrpg.android.habitica.events.commands.SellItemCommand;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuSelectionRunnable;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Item;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder> {

    private List<Item> itemList;

    public Boolean isHatching;

    public Context context;

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
        this.notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_item, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.bind(this.itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Item item;

        Resources resources;
        ItemItemBinding binding;

        public ItemViewHolder(View itemView) {
            super(itemView);

            resources = itemView.getResources();

            binding = DataBindingUtil.bind(itemView);

            itemView.setOnClickListener(this);
        }

        public void bind(Item item) {
            this.item = item;
            binding.setTitle(item.getText());
            if (item instanceof QuestContent) {
                binding.setImageNamed("inventory_quest_scroll_"+item.getKey());
            } else {
                String type = "";
                if (item instanceof Egg) {
                    type = "Egg";
                } else if (item instanceof Food) {
                    type = "Food";
                } else if (item instanceof HatchingPotion) {
                    type = "HatchingPotion";
                }
                binding.setImageNamed("Pet_"+type+"_"+item.getKey());
            }
            binding.setValue(item.getOwned().toString());
        }

        @Override
        public void onClick(View v) {
            if (!isHatching) {
                BottomSheetMenu menu = new BottomSheetMenu(context);
                if (!(item instanceof QuestContent)) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.sell, item.getValue()), true));
                }
                if (item instanceof Egg) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.hatch_with_potion)));
                } else if (item instanceof Food) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.feed_to_pet)));
                } else if (item instanceof HatchingPotion) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.hatch_egg)));
                } else if (item instanceof QuestContent) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.invite_party)));
                }
                menu.setSelectionRunnable(new BottomSheetMenuSelectionRunnable() {
                    @Override
                    public void selectedItemAt(Integer index) {
                        if (!(item instanceof QuestContent) && index == 0) {
                            SellItemCommand event = new SellItemCommand();
                            event.item = item;
                            EventBus.getDefault().post(event);
                            if (item.getOwned() > 1) {
                                item.setOwned(item.getOwned()-1);
                                notifyItemChanged(getAdapterPosition());
                            } else {
                                itemList.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());
                            }

                            return;
                        }
                        if (item instanceof Egg) {
                        } else if (item instanceof Food) {
                        } else if (item instanceof HatchingPotion) {
                        } else if (item instanceof QuestContent) {
                            InvitePartyToQuestCommand event = new InvitePartyToQuestCommand();
                            event.questKey = item.getKey();
                            EventBus.getDefault().post(event);
                        }
                    }
                });
                menu.show();
            }

        }
    }
}
