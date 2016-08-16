package com.habitrpg.android.habitica.ui.fragments.inventory.customization;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.adapter.CustomizationRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Preferences;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by viirus on 12/01/16.
 */
public class AvatarCustomizationFragment extends BaseMainFragment {

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
        recyclerView.addItemDecoration(new MarginDecoration(activity));

        recyclerView.setAdapter(adapter);
        this.loadCustomizations();

        this.updateActiveCustomization();
        this.adapter.userSize = this.user.getPreferences().getSize();
        this.adapter.hairColor = this.user.getPreferences().getHair().getColor();
        this.adapter.gemBalance = user.getBalance() * 4;

        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void loadCustomizations() {
        if (user == null || adapter == null) {
            return;
        }

        Where<Customization> select = new Select()
                .from(Customization.class)
                .where(Condition.column("type").eq(this.type))
                .and(Condition.CombinedCondition.begin(Condition.column("purchased").eq(true))
                        .or(Condition.column("price").eq(0))
                        .or(Condition.column("price").isNull())
                        .or(Condition.CombinedCondition.begin(
                                Condition.CombinedCondition.begin(Condition.column("availableUntil").isNull())
                                        .or(Condition.column("availableUntil").greaterThanOrEq(new Date().getTime())))
                                .and(Condition.CombinedCondition.begin(Condition.column("availableFrom").isNull())
                                        .or(Condition.column("availableFrom").lessThanOrEq(new Date().getTime()))
                                )
                        )
                );
        if (this.category != null) {
            select = select.and(Condition.column("category").eq(this.category));
        }
        if (this.type != null && this.type.equals("background")) {
            select.orderBy(OrderBy.columns("customizationSetName").descending());
        } else {
            select.orderBy(true, "customizationSet");
        }

        List<Customization> customizations = select.queryList();
        adapter.setCustomizationList(customizations);
    }

    private void setGridSpanCount(int width) {
        float itemWidth;
        if (this.type != null && this.type.equals("background")) {
            itemWidth = getContext().getResources().getDimension(R.dimen.avatar_width);
        } else {
            itemWidth = getContext().getResources().getDimension(R.dimen.customization_width);
        }

        int spanCount = (int) (width / itemWidth);
        if (spanCount == 0) {
            spanCount = 1;
        }
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public void updateUserData(HabitRPGUser user) {
        super.updateUserData(user);
        this.adapter.gemBalance = user.getBalance() * 4;
        this.updateActiveCustomization();
        if (adapter.getCustomizationList() != null) {
            List<String> ownedCustomizations = new ArrayList<>();
            for (Customization customization : user.getPurchased().customizations) {
                if (customization.getType().equals(this.type)) {
                    ownedCustomizations.add(customization.getId());
                }
            }
            adapter.updateOwnership(ownedCustomizations);
        } else {
            this.loadCustomizations();
        }
    }

    private void updateActiveCustomization() {
        Preferences prefs = this.user.getPreferences();
        if (this.type == null) {
            return;
        }
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
