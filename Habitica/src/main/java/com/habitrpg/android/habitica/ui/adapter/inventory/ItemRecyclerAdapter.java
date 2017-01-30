package com.habitrpg.android.habitica.ui.adapter.inventory;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ItemItemBinding;
import com.habitrpg.android.habitica.events.OpenMysteryItemEvent;
import com.habitrpg.android.habitica.events.ReloadContentEvent;
import com.habitrpg.android.habitica.events.commands.FeedCommand;
import com.habitrpg.android.habitica.events.commands.HatchingCommand;
import com.habitrpg.android.habitica.events.commands.InvitePartyToQuestCommand;
import com.habitrpg.android.habitica.events.commands.SellItemCommand;
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem;
import com.magicmicky.habitrpgwrapper.lib.models.ShopItem;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Item;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.SpecialItem;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder> {

    public Boolean isHatching;
    public Boolean isFeeding;
    public Boolean showShopAd;
    public String shopIdentifier;
    public Item hatchingItem;
    public Pet feedingPet;
    public ItemRecyclerFragment fragment;
    public HashMap<String, Integer> ownedPets;
    public Context context;
    private List<Item> itemList;

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

    public void openedMysteryItem(int numberLeft) {
        int itemPos = 0;
        for (Object obj : itemList) {
            if (obj.getClass().equals(SpecialItem.class)) {
                SpecialItem item = (SpecialItem) obj;
                    if (item.isMysteryItem) {
                        item.setOwned(numberLeft);
                        break;
                    }
            }
            itemPos++;
        }
        notifyItemChanged(itemPos);
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

        public Boolean isPetOwned() {
            String petKey;
            if (item instanceof Egg) {
                petKey = item.getKey() + "-" + hatchingItem.getKey();
            } else {
                petKey = hatchingItem.getKey() + "-" + item.getKey();
            }
            return ownedPets != null && ownedPets.containsKey(petKey) && ownedPets.get(petKey) > 0;
        }

        public void bind(Item item) {
            this.item = item;
            binding.setTitle(item.getText());

            if (item.getText() == null) {
                ReloadContentEvent event = new ReloadContentEvent();
                EventBus.getDefault().post(event);
            }

            binding.setDisabled(false);
            if (item instanceof QuestContent) {
                binding.setImageNamed("inventory_quest_scroll_" + item.getKey());
            } else if (item instanceof SpecialItem) {
                binding.setImageNamed(item.getKey());
            } else {
                String type = "";
                if (item instanceof Egg) {
                    type = "Egg";
                } else if (item instanceof Food) {
                    type = "Food";
                } else if (item instanceof HatchingPotion) {
                    type = "HatchingPotion";
                }
                binding.setImageNamed("Pet_" + type + "_" + item.getKey());

                if (isHatching != null && isHatching) {
                    this.binding.setDisabled(this.isPetOwned());
                }
            }
            binding.setValue(item.getOwned().toString());
        }

        @Override
        public void onClick(View v) {
            if (!isHatching && !isFeeding) {
                BottomSheetMenu menu = new BottomSheetMenu(context);
                if (!(item instanceof QuestContent) && !(item instanceof  SpecialItem)) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.sell, item.getValue()), true));
                }
                if (item instanceof Egg) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.hatch_with_potion)));
                } else if (item instanceof HatchingPotion) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.hatch_egg)));
                } else if (item instanceof QuestContent) {
                    menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.invite_party)));
                } else if (item instanceof SpecialItem) {
                    SpecialItem specialItem = (SpecialItem)item;
                    if (specialItem.isMysteryItem && specialItem.getOwned() > 0) {
                        menu.addMenuItem(new BottomSheetMenuItem(context.getString(R.string.open)));
                    }
                }
                menu.setSelectionRunnable(index -> {
                    if (!((item instanceof QuestContent) || (item instanceof SpecialItem)) && index == 0) {
                        SellItemCommand event = new SellItemCommand();
                        event.item = item;
                        EventBus.getDefault().post(event);
                        if (item.getOwned() > 1) {
                            item.setOwned(item.getOwned() - 1);
                            notifyItemChanged(getAdapterPosition());
                        } else {
                            if (getAdapterPosition() >= 0) {
                                itemList.remove(getAdapterPosition());
                            }
                            notifyItemRemoved(getAdapterPosition());
                        }

                        return;
                    }
                    if (item instanceof Egg) {
                        HatchingCommand event = new HatchingCommand();
                        event.usingEgg = (Egg) item;
                        EventBus.getDefault().post(event);
                    } else if (item instanceof Food) {
                        FeedCommand event = new FeedCommand();
                        event.usingFood = (Food) item;
                        EventBus.getDefault().post(event);
                    } else if (item instanceof HatchingPotion) {
                        HatchingCommand event = new HatchingCommand();
                        event.usingHatchingPotion = (HatchingPotion) item;
                        EventBus.getDefault().post(event);
                    } else if (item instanceof QuestContent) {
                        InvitePartyToQuestCommand event = new InvitePartyToQuestCommand();
                        event.questKey = item.getKey();
                        EventBus.getDefault().post(event);
                    } else if (item instanceof SpecialItem) {
                        EventBus.getDefault().post(new OpenMysteryItemEvent());
                    }
                });
                menu.show();
            } else if (isHatching) {
                if (this.isPetOwned()) {
                    return;
                }
                if (item instanceof Egg) {
                    HatchingCommand event = new HatchingCommand();
                    event.usingEgg = (Egg) item;
                    event.usingHatchingPotion = (HatchingPotion) hatchingItem;
                    EventBus.getDefault().post(event);
                } else if (item instanceof HatchingPotion) {
                    HatchingCommand event = new HatchingCommand();
                    event.usingHatchingPotion = (HatchingPotion) item;
                    event.usingEgg = (Egg) hatchingItem;
                    EventBus.getDefault().post(event);
                }
                fragment.dismiss();
            } else if (isFeeding) {
                FeedCommand event = new FeedCommand();
                event.usingPet = feedingPet;
                event.usingFood = (Food) item;
                EventBus.getDefault().post(event);
                fragment.dismiss();
            }

        }
    }
}
