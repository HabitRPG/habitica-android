package com.habitrpg.android.habitica;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.habitrpg.android.habitica.userpicture.UserPictureRunnable;
import com.instabug.wrapper.support.activity.InstabugAppCompatActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Negue on 14.09.2015.
 */
public abstract class AvatarActivityBase extends InstabugAppCompatActivity {

    //region View Elements
    @InjectView(R.id.viewpager)
    ViewPager viewPager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.detail_tabs)
    TabLayout detail_tabs;

    @InjectView(R.id.avatar_with_bars)
    View avatar_with_bars;

    @InjectView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    TextView titleTextView;

    AccountHeader accountHeader;
    Drawer drawer;
    //endregion

    protected HostConfig hostConfig;
    protected HabitRPGUser User;

    AvatarWithBarsViewModel avatarInHeader;

    protected abstract int getLayoutRes();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());

        // Inject Controls
        ButterKnife.inject(this);

        // Receive Events
        EventBus.getDefault().register(this);

        // Initialize Crashlytics
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlytics);

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(false);
            }

//            toolbar.setPadding(0, getResources().getDimensionPixelSize(R.dimen.tool_bar_top_padding), 0, 0);
        }

        viewPager.setBackgroundColor(getResources().getColor(R.color.white));

        avatarInHeader = new AvatarWithBarsViewModel(this, avatar_with_bars);

        accountHeader = MainDrawerBuilder.CreateDefaultAccountHeader(this).build();
        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar, accountHeader)
                .build();

//        titleTextView = new TextView(this);
//        titleTextView.setTextAppearance(this, android.R.style.TextAppearance_Material_Widget_ActionBar_Title_Inverse);
//        titleTextView.setPadding(0,16,0,0);
//        toolbar.addView(titleTextView);
    }

    protected void setTitle(String text){
        toolbar.setTitle(text);
    }

    public void updateSidebar() {
        final IProfile profile = accountHeader.getProfiles().get(0);
        profile.withName(User.getProfile().getName());
        new UserPicture(User, this, true, false).setPictureWithRunnable(new UserPictureRunnable() {
            public void run(Bitmap avatar) {
                profile.withIcon(avatar);
                accountHeader.updateProfile(profile);
            }
        });
        accountHeader.updateProfile(profile);
    }
}
