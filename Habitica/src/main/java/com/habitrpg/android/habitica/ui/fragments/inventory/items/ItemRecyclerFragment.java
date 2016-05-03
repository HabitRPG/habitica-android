package com.habitrpg.android.habitica.ui.fragments.inventory.items;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.ContentReloadedEvent;
import com.habitrpg.android.habitica.ui.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.RecyclerViewEmptySupport;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Item;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.Subscribe;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ItemRecyclerFragment extends BaseFragment {
    @Bind(R.id.recyclerView)
    public RecyclerViewEmptySupport recyclerView;

    @Bind(R.id.empty_view)
    public TextView emptyView;

    @Bind(R.id.titleTextView)
    public TextView titleView;
    public ItemRecyclerAdapter adapter;
    public String itemType;
    public String itemTypeText;

    public Boolean isHatching;
    public Boolean isFeeding;
    public Item hatchingItem;
    public Pet feedingPet;
    public HashMap<String, Integer> ownedPets;

    private static final String ITEM_TYPE_KEY = "CLASS_TYPE_KEY";
    LinearLayoutManager layoutManager = null;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_items, container, false);

            ButterKnife.bind(this, view);

            recyclerView.setEmptyView(emptyView);
            emptyView.setText(getString(R.string.empty_items, itemTypeText));

            android.support.v4.app.FragmentActivity context = getActivity();

            layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            if (layoutManager == null) {
                layoutManager = new LinearLayoutManager(context);

                recyclerView.setLayoutManager(layoutManager);
            }

            adapter = (ItemRecyclerAdapter)recyclerView.getAdapter();
            if (adapter == null) {
                adapter = new ItemRecyclerAdapter();
                adapter.context = this.getActivity();
                adapter.isHatching = this.isHatching;
                adapter.isFeeding = this.isFeeding;
                adapter.fragment = this;
                adapter.ownedPets = this.ownedPets;
                if (this.hatchingItem != null) {
                    adapter.hatchingItem = this.hatchingItem;
                }
                if (this.feedingPet != null) {
                    adapter.feedingPet = this.feedingPet;
                }
                recyclerView.setAdapter(adapter);
            }
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }

        if (savedInstanceState != null){
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "");
        }

        if (this.isHatching) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.titleView.setText(getString(R.string.hatch_with, this.hatchingItem.getText()));
            this.titleView.setVisibility(View.VISIBLE);
        } else if (this.isFeeding) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.titleView.setText(getString(R.string.dialog_feeding, this.feedingPet.getColorText(), this.feedingPet.getAnimalText()));
            this.titleView.setVisibility(View.VISIBLE);
        } else {
            this.titleView.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.loadItems();
    }

    @Override
    public void onResume() {
        if (this.isHatching) {
            ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        }

        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ITEM_TYPE_KEY, this.itemType);
    }

    @Subscribe
    public void reloadedContent(ContentReloadedEvent event) {
        this.loadItems();
    }

    private void loadItems() {
        From from = null;
        if (this.itemType == null) {
            return;
        }
        switch (this.itemType) {
            case "eggs":
                from = new Select().from(Egg.class);
                break;
            case "hatchingPotions":
                from = new Select().from(HatchingPotion.class);
                break;
            case "food":
                from = new Select().from(Food.class);
                break;
            case "quests":
                from = new Select().from(QuestContent.class);
        }

        if (from != null) {
            List<Item> items = from.where(Condition.column("owned").greaterThan(0)).queryList();
            adapter.setItemList(items);
        }
    }
}
