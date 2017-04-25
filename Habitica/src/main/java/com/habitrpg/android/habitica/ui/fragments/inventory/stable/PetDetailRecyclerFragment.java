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
import com.habitrpg.android.habitica.events.commands.FeedCommand;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.adapter.inventory.PetDetailRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

public class PetDetailRecyclerFragment extends BaseMainFragment {
    private static final String ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY";

    @Inject
    InventoryRepository inventoryRepository;

    public RecyclerView recyclerView;
    public PetDetailRecyclerAdapter adapter;
    public String animalType;
    public String animalGroup;
    GridLayoutManager layoutManager = null;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.usesTabLayout = false;
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

            layoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new MarginDecoration(getActivity()));

            adapter = (PetDetailRecyclerAdapter) recyclerView.getAdapter();
            if (adapter == null) {
                adapter = new PetDetailRecyclerAdapter(null, true);
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
    public void onDestroy() {
        inventoryRepository.close();
        super.onDestroy();
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
        inventoryRepository.getPets(animalType, animalGroup).first().subscribe(adapter::updateData, ReactiveErrorHandler.handleEmptyError());
        inventoryRepository.getOwnedMounts(animalType, animalGroup).subscribe(adapter::setOwnedMounts, ReactiveErrorHandler.handleEmptyError());
    }

    @Subscribe
    public void showFeedingDialog(FeedCommand event) {
        if (event.usingPet == null || event.usingFood == null) {
            ItemRecyclerFragment fragment = new ItemRecyclerFragment();
            fragment.feedingPet = event.usingPet;
            fragment.isFeeding = true;
            fragment.isHatching = false;
            fragment.itemType = "food";
            fragment.itemTypeText = getString(R.string.food);
            fragment.show(getFragmentManager(), "feedDialog");
        }
    }

    @Override
    public void updateUserData(User user) {
        super.updateUserData(user);
    }

    @Override
    public String customTitle() {
        return getString(R.string.pets);
    }
}
