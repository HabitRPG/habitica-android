package com.habitrpg.android.habitica.ui.fragments.setup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.SetupActivity;
import com.habitrpg.android.habitica.ui.adapter.setup.CustomizationSetupAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AvatarSetupFragment extends BaseFragment {

    View view;
    public SetupActivity activity;
    private HabitRPGUser user;
    public int width;


    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.avatarView)
    ImageView avatarView;

    CustomizationSetupAdapter adapter;
    GridLayoutManager layoutManager;
    UserPicture userPicture;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_setup_avatar, container, false);
        }

        ButterKnife.bind(this, view);
        this.adapter = new CustomizationSetupAdapter();
        if (this.user != null) {
            this.adapter.userSize = this.user.getPreferences().getSize();
        } else {
            this.adapter.userSize = "slim";
        }
        this.adapter.user = this.user;
        this.layoutManager = new GridLayoutManager(activity, 2);
        this.layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == 0) {
                    return layoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
        this.recyclerView.setLayoutManager(this.layoutManager);
        this.recyclerView.addItemDecoration(new MarginDecoration(this.activity));

        this.recyclerView.setAdapter(this.adapter);
        this.loadCustomizations();

        userPicture = new UserPicture(this.activity, true, true);
        if (this.user != null) {
            this.updateAvatar();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setGridSpanCount(width);
    }

    private void loadCustomizations() {
        if(this.user == null || this.adapter == null){
            return;
        }

        Where<Customization> select = new Select()
                .from(Customization.class)
                .where(Condition.CombinedCondition.begin(Condition.column("purchased").eq(true))
                                .or(Condition.column("price").eq(0))
                                .or(Condition.column("price").isNull())
                );

        List<Customization> customizations = select.queryList();
        this.adapter.setCustomizationList(customizations);
    }

    private void setGridSpanCount(int width) {
        float itemWidth = getContext().getResources().getDimension(R.dimen.customization_width);

        int spanCount = (int) (width / itemWidth);
        this.layoutManager.setSpanCount(spanCount);
    }

    public void setUser(HabitRPGUser user) {
        this.user = user;
        if (this.userPicture != null) {
            this.updateAvatar();
        }
        if (this.adapter != null) {
            this.adapter.user = user;
            this.adapter.notifyDataSetChanged();
        }
    }

    private void updateAvatar() {
        this.userPicture.setUser(this.user);
        this.userPicture.setPictureOn(this.avatarView);
    }

}
