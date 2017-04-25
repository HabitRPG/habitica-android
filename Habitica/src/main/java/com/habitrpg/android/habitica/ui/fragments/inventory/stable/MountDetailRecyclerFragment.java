package com.habitrpg.android.habitica.ui.fragments.inventory.stable;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.ui.adapter.inventory.MountDetailRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;

import java.util.List;

import javax.inject.Inject;

public class MountDetailRecyclerFragment extends BaseMainFragment {
    private static final String ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY";

    @Inject
    InventoryRepository inventoryRepository;

    public RecyclerView recyclerView;
    public MountDetailRecyclerAdapter adapter;
    public String animalType;
    public String animalGroup;
    public List<Mount> animals;
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

            adapter = (MountDetailRecyclerAdapter) recyclerView.getAdapter();
            if (adapter == null) {
                adapter = new MountDetailRecyclerAdapter();
                adapter.context = this.getActivity();
                adapter.itemType = this.animalType;
                recyclerView.setAdapter(adapter);
                this.loadItems();

            }
        }

        if (savedInstanceState != null) {
            this.animalType = savedInstanceState.getString(ANIMAL_TYPE_KEY, "");
        }

        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View finalView = view;
        finalView.post(() -> setGridSpanCount(finalView.getWidth()));
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
        inventoryRepository.getMounts().subscribe(mounts -> {
            adapter.setItemList(mounts);
            animals = mounts;
        });
    }

    @Override
    public String customTitle() {
        return getString(R.string.mounts);
    }
}
