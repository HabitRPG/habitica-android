package com.habitrpg.android.habitica.ui.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.adapter.CustomizationRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Preferences;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by viirus on 12/01/16.
 */
public class AvatarCustomizationFragment extends BaseFragment {

    public String type;
    public String group;
    public String activeCustomization;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    CustomizationRecyclerViewAdapter adapter;
    GridLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        ButterKnife.bind(this, v);
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

        return v;
    }

    private void loadCustomizations() {
        if(user == null || adapter == null){
            return;
        }

        Where<Customization> select = new Select()
                .from(Customization.class)
                .where(Condition.column("type").eq(this.type))
                .and(Condition.CombinedCondition.begin(Condition.column("purchasable").eq(true))
                        .or(Condition.column("purchased").eq(true))
                        .or(Condition.column("price").eq(0))
                        .or(Condition.column("price").isNull()));
        if (this.group != null) {
            select = select.and(Condition.column("group").eq(this.group));
        }
        select.orderBy(true, "set", "identifier");

        List<Customization> customizations = select.queryList();
        adapter.setCustomizationList(customizations);
    }

    private void setGridSpanCount(int width) {
        int spanCount = width / 100;
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public void updateUserData(HabitRPGUser user) {
        super.updateUserData(user);
        this.updateActiveCustomization();
    }

    private void updateActiveCustomization() {
        Preferences prefs = this.user.getPreferences();
        switch (this.type) {
            case "skin":
                this.activeCustomization = prefs.getSkin();
                break;
            case "shirt":
                this.activeCustomization = prefs.getShirt();
                break;
            case "hair":
                switch (this.group) {
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
