package com.habitrpg.android.habitica.ui.fragments.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.adapter.inventory.PetDetailRecyclerAdapter;
import com.habitrpg.android.habitica.ui.adapter.inventory.StableRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Animal;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Mount;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetDetailRecyclerFragment extends BaseMainFragment {
    public RecyclerView recyclerView;
    public PetDetailRecyclerAdapter adapter;
    public String animalType;
    public List<Pet> animals;
    private static final String ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY";
    GridLayoutManager layoutManager = null;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.usesTabLayout = false;
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

            android.support.v4.app.FragmentActivity context = getActivity();

            layoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new MarginDecoration(getActivity()));

            adapter = (PetDetailRecyclerAdapter)recyclerView.getAdapter();
            if (adapter == null) {
                adapter = new PetDetailRecyclerAdapter();
                adapter.context = this.getActivity();
                adapter.itemType = this.animalType;
                recyclerView.setAdapter(adapter);
                this.loadItems();

            }
        }

        if (savedInstanceState != null){
            this.animalType = savedInstanceState.getString(ANIMAL_TYPE_KEY, "");
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
        outState.putString(ANIMAL_TYPE_KEY, this.animalType);
    }



    private void setGridSpanCount(int width) {
        float itemWidth;
        itemWidth = getContext().getResources().getDimension(R.dimen.pet_width);

        int spanCount = (int) (width / itemWidth);
        if (spanCount == 0) {
            spanCount = 1;
        }
        layoutManager.setSpanCount(spanCount);
        layoutManager.requestLayout();
    }

    private void loadItems() {
        Runnable itemsRunnable = new Runnable() {
            @Override
            public void run() {
                List<Pet> items = new Select().from(Pet.class).where(Condition.column("animal").eq(animalType)).orderBy(true, "color").queryList();
                adapter.setItemList(items);
                animals = items;
                adapter.setOwnedMapping(user.getItems().getPets());
            }
        };
        itemsRunnable.run();

    }
}
