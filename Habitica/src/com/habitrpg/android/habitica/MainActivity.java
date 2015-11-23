package com.habitrpg.android.habitica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.habitrpg.android.habitica.userpicture.UserPictureRunnable;
import com.instabug.wrapper.support.activity.InstabugAppCompatActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.fabric.sdk.android.Fabric;

/**
 * Created by admin on 18/11/15.
 */
public class MainActivity extends InstabugAppCompatActivity implements HabitRPGUserCallback.OnUserReceived, FlowContentObserver.OnSpecificModelStateChangedListener {

    BaseFragment activeFragment;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.detail_tabs)
    TabLayout detail_tabs;

    @InjectView(R.id.avatar_with_bars)
    View avatar_with_bars;

    @InjectView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    AccountHeader accountHeader;
    Drawer drawer;

    protected HostConfig hostConfig;
    protected HabitRPGUser user;

    AvatarWithBarsViewModel avatarInHeader;

    FlowContentObserver observer;
    APIHelper mAPIHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inject Controls
        ButterKnife.inject(this);

        // Initialize Crashlytics
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlytics);

        this.hostConfig = PrefsActivity.fromContext(this);
        if (hostConfig == null || hostConfig.getApi() == null || hostConfig.getApi().equals("") || hostConfig.getUser() == null || hostConfig.getUser().equals("")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        this.mAPIHelper = new APIHelper(this, hostConfig);

        new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).async().querySingle(userTransactionListener);
        this.observer = new FlowContentObserver();
        this.observer.registerForContentChanges(this.getApplicationContext(), HabitRPGUser.class);

        this.observer.addSpecificModelChangeListener(this);


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

        }

        avatarInHeader = new AvatarWithBarsViewModel(this, avatar_with_bars);
        accountHeader = MainDrawerBuilder.CreateDefaultAccountHeader(this).build();
        drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar, accountHeader)
                .build();

        // Create a new Fragment to be placed in the activity layout
        TasksFragment tasksFragment = new TasksFragment();
        tasksFragment.fragmentSidebarPosition = 0;
        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        this.displayFragment(tasksFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));
        SetUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void displayFragment(BaseFragment fragment) {
        fragment.setArguments(getIntent().getExtras());
        fragment.mAPIHelper = mAPIHelper;
        fragment.setUser(user);
        fragment.setActivity(this);
        fragment.setTabLayout(detail_tabs);

        if (getSupportFragmentManager().getFragments() == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        }
    }

    private TransactionListener<HabitRPGUser> userTransactionListener = new TransactionListener<HabitRPGUser>() {
        @Override
        public void onResultReceived(HabitRPGUser habitRPGUser) {
            user = habitRPGUser;
            SetUserData();
        }

        @Override
        public boolean onReady(BaseTransaction<HabitRPGUser> baseTransaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<HabitRPGUser> baseTransaction, HabitRPGUser habitRPGUser) {
            return true;
        }
    };

    @Override
    public void onModelStateChanged(Class<? extends Model> aClass, BaseModel.Action action, String s, String s1) {
        if (aClass != HabitRPGUser.class)
            return;


        new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).async().querySingle(userTransactionListener);
    }

    private void SetUserData() {
        if (user != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateHeader();
                    updateSidebar();
                    if (activeFragment != null) {
                        activeFragment.updateUserData(user);
                    }
                }
            });
        }
    }

    private void updateUserAvatars() {
        avatarInHeader.updateData(user);
    }

    private void updateHeader() {
        updateUserAvatars();
        setTitle(user.getProfile().getName());

        android.support.v7.app.ActionBarDrawerToggle actionBarDrawerToggle = drawer.getActionBarDrawerToggle();

        if (actionBarDrawerToggle != null) {
            actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        }
    }

    public void updateSidebar() {
        final IProfile profile = accountHeader.getProfiles().get(0);
        if (user.getAuthentication() != null) {
            if (user.getAuthentication().getLocalAuthentication() != null) {
                profile.withEmail(user.getAuthentication().getLocalAuthentication().getEmail());
            }
        }
        profile.withName(user.getProfile().getName());
        new UserPicture(user, this, true, false).setPictureWithRunnable(new UserPictureRunnable() {
            public void run(Bitmap avatar) {
                profile.withIcon(avatar);
                accountHeader.updateProfile(profile);
            }
        });
        accountHeader.updateProfile(profile);
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
    }

    @Override
    public void onUserFail() {

    }

    public void setActiveFragment(BaseFragment fragment) {
        this.activeFragment = fragment;
        this.drawer.setSelectionAtPosition(this.activeFragment.fragmentSidebarPosition, false);
    }
}
