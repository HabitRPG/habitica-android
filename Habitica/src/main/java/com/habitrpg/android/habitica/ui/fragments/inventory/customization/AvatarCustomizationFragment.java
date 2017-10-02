package com.habitrpg.android.habitica.ui.fragments.inventory.customization;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.CustomizationRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.ui.adapter.CustomizationRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvatarCustomizationFragment extends BaseMainFragment {

    @Inject
    CustomizationRepository customizationRepository;

    public String type;
    public String category;
    public String activeCustomization;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    CustomizationRecyclerViewAdapter adapter;
    GridLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        unbinder = ButterKnife.bind(this, view);
        adapter = new CustomizationRecyclerViewAdapter();

        compositeSubscription.add(adapter.getSelectCustomizationEvents()
                .flatMap(customization -> {
                    String updatePath = "preferences." + customization.getType();
                    if (customization.getCategory() != null) {
                        updatePath = updatePath + "." + customization.getCategory();
                    }
                    return userRepository.updateUser(user, updatePath, customization.getIdentifier());
                })
                .subscribe(user1 -> {}, RxErrorHandler.handleEmptyError()));
        compositeSubscription.add(adapter.getUnlockCustomizationEvents()
                .flatMap(customization -> userRepository.unlockPath(user, customization))
                .subscribe(unlockResponse -> {}, RxErrorHandler.handleEmptyError()));
        compositeSubscription.add(adapter.getUnlockSetEvents()
                .flatMap(set -> userRepository.unlockPath(user, set))
                .subscribe(unlockResponse -> {}, RxErrorHandler.handleEmptyError()));

        layoutManager = new GridLayoutManager(activity, 2);
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
        setGridSpanCount(container.getWidth());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new MarginDecoration(getContext()));

        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new SafeDefaultItemAnimator());
        this.loadCustomizations();

        this.updateActiveCustomization();
        if (this.user != null) {
            this.adapter.userSize = this.user.getPreferences().getSize();
            this.adapter.hairColor = this.user.getPreferences().getHair().getColor();
            this.adapter.gemBalance = user.getBalance() * 4;
        }

        return view;
    }

    @Override
    public void onDestroy() {
        customizationRepository.close();
        super.onDestroy();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void loadCustomizations() {
        if (user == null || adapter == null) {
            return;
        }
        customizationRepository.getCustomizations(type, category, true).subscribe(adapter::setCustomizationList, RxErrorHandler.handleEmptyError());
    }

    private void setGridSpanCount(int width) {
        float itemWidth;
        int spanCount = 0;
        if (getContext() != null && getContext().getResources() != null) {
            if (this.type != null && this.type.equals("background")) {
                itemWidth = getContext().getResources().getDimension(R.dimen.avatar_width);
            } else {
                itemWidth = getContext().getResources().getDimension(R.dimen.customization_width);
            }
            spanCount = (int) (width / itemWidth);
        }
        if (spanCount == 0) {
            spanCount = 1;
        }
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public void updateUserData(User user) {
        super.updateUserData(user);
        if (adapter != null) {
            this.adapter.gemBalance = user.getBalance() * 4;
            this.updateActiveCustomization();
            if (adapter.getCustomizationList() != null) {
                List<String> ownedCustomizations = new ArrayList<>();
                if (user.getPurchased() != null && user.getPurchased().customizations != null) {
                    for (Customization customization : user.getPurchased().customizations) {
                        if (customization.getType().equals(this.type)) {
                            ownedCustomizations.add(customization.getId());
                        }
                    }
                }
                adapter.updateOwnership(ownedCustomizations);
            } else {
                this.loadCustomizations();
            }
        }
    }

    private void updateActiveCustomization() {
        if (this.type == null || this.user == null || this.user.getPreferences() == null) {
            return;
        }
        Preferences prefs = this.user.getPreferences();
        switch (this.type) {
            case "skin":
                this.activeCustomization = prefs.getSkin();
                break;
            case "shirt":
                this.activeCustomization = prefs.getShirt();
                break;
            case "background":
                this.activeCustomization = prefs.getBackground();
                break;
            case "hair":
                switch (this.category) {
                    case "bangs":
                        this.activeCustomization = String.valueOf(prefs.getHair().getBangs());
                        break;
                    case "base":
                        this.activeCustomization = String.valueOf(prefs.getHair().getBase());
                        break;
                    case "color":
                        this.activeCustomization = prefs.getHair().getColor();
                        break;
                    case "flower":
                        this.activeCustomization = String.valueOf(prefs.getHair().getFlower());
                        break;
                    case "beard":
                        this.activeCustomization = String.valueOf(prefs.getHair().getBeard());
                        break;
                    case "mustache":
                        this.activeCustomization = String.valueOf(prefs.getHair().getMustache());
                        break;
                }
        }
        this.adapter.setActiveCustomization(this.activeCustomization);
    }
}
