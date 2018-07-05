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
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.ui.adapter.inventory.MountDetailRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;

import javax.inject.Inject;

public class MountDetailRecyclerFragment extends BaseMainFragment {
    private static final String ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY";

    @Inject
    InventoryRepository inventoryRepository;

    public RecyclerView recyclerView;
    public MountDetailRecyclerAdapter adapter;
    public String animalType;
    public String animalGroup;
    GridLayoutManager layoutManager = null;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.usesTabLayout = false;
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new MarginDecoration(getActivity()));

        adapter = (MountDetailRecyclerAdapter) recyclerView.getAdapter();
        if (adapter == null) {
            adapter = new MountDetailRecyclerAdapter(null, true);
            adapter.context = this.getActivity();
            adapter.itemType = this.animalType;
            recyclerView.setAdapter(adapter);
            recyclerView.setItemAnimator(new SafeDefaultItemAnimator());
            this.loadItems();

            getCompositeSubscription().add(adapter.getEquipEvents()
                    .flatMap(key -> inventoryRepository.equip(user, "mount", key))
                    .subscribe(items -> {}, RxErrorHandler.handleEmptyError()));
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
        int spanCount = 0;
        if (getContext() != null && getContext().getResources() != null) {
            float itemWidth;
            itemWidth = getContext().getResources().getDimension(R.dimen.pet_width);

            spanCount = (int) (width / itemWidth);
        }
        if (spanCount == 0) {
            spanCount = 1;
        }
        layoutManager.setSpanCount(spanCount);
        layoutManager.requestLayout();
    }

    private void loadItems() {
        if (animalType != null && animalGroup != null) {
            inventoryRepository.getMounts(animalType, animalGroup).firstElement().subscribe(adapter::updateData, RxErrorHandler.handleEmptyError());
        }
    }

    @Override
    public String customTitle() {
        if (!isAdded()) {
            return "";
        }
        return getString(R.string.mounts);
    }
}
