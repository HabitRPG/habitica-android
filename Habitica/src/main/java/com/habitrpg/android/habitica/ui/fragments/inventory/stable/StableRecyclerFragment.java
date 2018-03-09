package com.habitrpg.android.habitica.ui.fragments.inventory.stable;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.inventory.Animal;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.adapter.inventory.StableRecyclerAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.habitrpg.android.habitica.ui.helpers.RecyclerViewEmptySupport;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

public class StableRecyclerFragment extends BaseFragment {
    private static final String ITEM_TYPE_KEY = "CLASS_TYPE_KEY";

    @Inject
    InventoryRepository inventoryRepository;

    @BindView(R.id.recyclerView)
    public RecyclerViewEmptySupport recyclerView;
    @BindView(R.id.emptyView)
    public TextView emptyView;
    public StableRecyclerAdapter adapter;
    public String itemType;
    public String itemTypeText;
    public User user;
    GridLayoutManager layoutManager = null;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        setUnbinder(ButterKnife.bind(this, view));

        recyclerView.setEmptyView(emptyView);
        emptyView.setText(getString(R.string.empty_items, itemTypeText));

        layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == 0) {
                    return layoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new MarginDecoration(getActivity()));


        adapter = (StableRecyclerAdapter) recyclerView.getAdapter();
        if (adapter == null) {
            adapter = new StableRecyclerAdapter();
            adapter.setActivity((MainActivity) this.getActivity());
            adapter.setItemType(this.itemType);
            recyclerView.setAdapter(adapter);
            recyclerView.setItemAnimator(new SafeDefaultItemAnimator());
        }

        if (savedInstanceState != null) {
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "");
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
        this.loadItems();
        final View finalView = view;
        finalView.post(() -> setGridSpanCount(finalView.getWidth()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ITEM_TYPE_KEY, this.itemType);
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
    }

    private void loadItems() {
        Observable<? extends Animal> observable;

        if ("pets".equals(itemType)) {
            observable = inventoryRepository.getPets().first().flatMap(Observable::from);
        } else {
            observable = inventoryRepository.getMounts().first().flatMap(Observable::from);
        }

        observable.toList().flatMap(unsortedAnimals -> {
            List<Object> items = new ArrayList<>();
            if (unsortedAnimals.size() == 0) {
                return Observable.just(items);
            }
            String lastSectionTitle = "";

            Animal lastAnimal = unsortedAnimals.get(0);
            for (Animal animal : unsortedAnimals) {
                if (!animal.getAnimal().equals(lastAnimal.getAnimal()) || animal == unsortedAnimals.get(unsortedAnimals.size()-1)) {
                    if (!((lastAnimal.getAnimalGroup().equals("premiumPets") || lastAnimal.getAnimalGroup().equals("specialPets")
                            || lastAnimal.getAnimalGroup().equals("specialMounts") || lastAnimal.getAnimalGroup().equals("premiumMounts"))
                            && lastAnimal.getNumberOwned() == 0)) {
                        items.add(lastAnimal);
                    }
                    lastAnimal = animal;
                }
                if (!animal.getAnimalGroup().equals(lastSectionTitle)) {
                    if (items.size() > 0 && items.get(items.size() - 1).getClass().equals(String.class)) {
                        items.remove(items.size() - 1);
                    }
                    items.add(animal.getAnimalGroup());
                    lastSectionTitle = animal.getAnimalGroup();
                }
                if (user != null && user.getItems() != null) {
                    switch (itemType) {
                        case "pets":
                            Pet pet = (Pet) animal;
                            if (pet.getTrained() > 0) {
                                lastAnimal.setNumberOwned(lastAnimal.getNumberOwned() + 1);
                            }
                            break;
                        case "mounts":
                            Mount mount = (Mount) animal;
                            if (mount.getOwned()) {
                                lastAnimal.setNumberOwned(lastAnimal.getNumberOwned() + 1);
                            }
                            break;
                    }
                }
            }
            return Observable.just(items);
        }).subscribe(items -> adapter.setItemList(items), RxErrorHandler.handleEmptyError());
    }
}
