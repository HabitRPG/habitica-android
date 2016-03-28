package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ItemImageRowBinding;
import com.habitrpg.android.habitica.databinding.ItemItemBinding;
import com.habitrpg.android.habitica.events.commands.UseSkillCommand;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.Quest;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Item;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder> {

    private List<Item> itemList;

    public Double mana;

    public Context context;

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
        this.notifyDataSetChanged();
    }

    public void setMana(Double mana) {
        this.mana = mana;
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

        }
    }
}
