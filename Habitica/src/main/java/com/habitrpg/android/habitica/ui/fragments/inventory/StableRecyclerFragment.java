package com.habitrpg.android.habitica.ui.fragments.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter;
import com.habitrpg.android.habitica.ui.adapter.inventory.StableRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Animal;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Item;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Mount;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StableRecyclerFragment extends BaseFragment {
    public RecyclerView recyclerView;
    public StableRecyclerAdapter adapter;
    public String itemType;
    public HabitRPGUser user;
    public List<Animal> animals;
    private static final String ITEM_TYPE_KEY = "CLASS_TYPE_KEY";
    GridLayoutManager layoutManager = null;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

            android.support.v4.app.FragmentActivity context = getActivity();

            layoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new MarginDecoration(getActivity()));

            adapter = (StableRecyclerAdapter)recyclerView.getAdapter();
            if (adapter == null) {
                adapter = new StableRecyclerAdapter();
                adapter.activity = (MainActivity)this.getActivity();
                adapter.itemType = this.itemType;
                recyclerView.setAdapter(adapter);
                this.loadItems();

            }
        }

        if (savedInstanceState != null){
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "");
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View finalView = view;
        finalView.post(new Runnable() {
            @Override
            public void run() {
                setGridSpanCount(finalView.getWidth());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ITEM_TYPE_KEY, this.itemType);
    }



    private void setGridSpanCount(int width) {
        float itemWidth;
        itemWidth = getContext().getResources().getDimension(R.dimen.pet_width);

        int spanCount = (int) (width / itemWidth);
        if (spanCount == 0) {
            spanCount = 1;
        }
        layoutManager.setSpanCount(spanCount);
    }

    private void loadItems() {
        Runnable itemsRunnable = new Runnable() {
            @Override
            public void run() {
                From from = null;
                switch (itemType) {
                    case "pets":
                        from = new Select().from(Pet.class);
                        break;
                    case "mounts":
                        from = new Select().from(Mount.class);
                        break;
                }

                if (from != null) {
                    List<Animal> items = from.where().orderBy(true, "animalGroup", "animal").groupBy("animal").queryList();
                    adapter.setItemList(items);
                    animals = items;
                    HashMap<String, Integer> ownedMap = new HashMap<>();
                    for (Animal animal : animals) {
                        ownedMap.put(animal.getAnimal(), 0);
                    }
                    switch (itemType) {
                        case "pets":
                            for (Map.Entry<String, Integer> pet : StableRecyclerFragment.this.user.getItems().getPets().entrySet()) {
                                if (pet.getValue() > 0) {
                                    ownedMap.put(pet.getKey().split("-")[0], ownedMap.get(pet.getKey().split("-")[0])+1);
                                }
                            }
                            break;
                        case "mounts":
                            for (Map.Entry<String, Boolean> mount : StableRecyclerFragment.this.user.getItems().getMounts().entrySet()) {
                                if (mount.getValue()) {
                                    ownedMap.put(mount.getKey().split("-")[0], ownedMap.get(mount.getKey().split("-")[0])+1);
                                }
                            }
                            break;
                    }
                    adapter.setOwnedMapping(ownedMap);
                }
            }
        };
        itemsRunnable.run();

    }
}
