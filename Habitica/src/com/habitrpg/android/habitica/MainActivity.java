package com.habitrpg.android.habitica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.habitrpg.android.habitica.userpicture.UserPictureRunnable;
import com.instabug.wrapper.support.activity.InstabugAppCompatActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends InstabugAppCompatActivity implements HabitRPGUserCallback.OnUserReceived {

    public enum SnackbarDisplayType {
        NORMAL, FAILURE, DROP
    }

    BaseFragment activeFragment;

    @InjectView(R.id.floating_menu_wrapper)
    FrameLayout floatingMenuWrapper;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.detail_tabs)
    TabLayout detail_tabs;

    @InjectView(R.id.avatar_with_bars)
    View avatar_with_bars;

    AccountHeader accountHeader;
    public Drawer drawer;

    protected HostConfig hostConfig;
    protected HabitRPGUser user;

    AvatarWithBarsViewModel avatarInHeader;

    APIHelper mAPIHelper;

    // Checkout needs to be in the Activity..
    public static ActivityCheckout checkout = null;

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
        HabiticaApplication.checkUserAuthentication(this, hostConfig);
        HabiticaApplication.ApiHelper = this.mAPIHelper = new APIHelper(this, hostConfig);

        new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).async().querySingle(userTransactionListener);


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

        drawer.setSelectionAtPosition(1);
        
        // Create Checkout

        checkout = Checkout.forActivity(this, HabiticaApplication.Instance.getCheckout());

        checkout.start();

        EventBus.getDefault().register(this);
    }


    private void saveLoginInformation(){
        HabiticaApplication.User = user;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        boolean ans = editor.putString(getString(R.string.SP_username), user.getAuthentication().getLocalAuthentication().getUsername())
                .putString(getString(R.string.SP_email), user.getAuthentication().getLocalAuthentication().getEmail())
                .commit();
        try{
            if(!ans) {
                throw new Exception("Shared Preferences Username and Email error");
            }
        }catch (Exception e){
            Log.e("SHARED PREFERENCES", e.getMessage());
        }
    }

    public void onEvent(OpenGemPurchaseFragmentCommand cmd){
        drawer.setSelection(MainDrawerBuilder.SIDEBAR_PURCHASE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAPIHelper.retrieveUser(new HabitRPGUserCallback(this));
        setUserData();
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
        fragment.setFloatingMenuWrapper(floatingMenuWrapper);

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
            setUserData();
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

    private void setUserData() {
        if (user != null) {
            Calendar mCalendar = new GregorianCalendar();
            TimeZone mTimeZone = mCalendar.getTimeZone();
            long offset = -TimeUnit.MINUTES.convert(mTimeZone.getRawOffset(), TimeUnit.MILLISECONDS);
            if (offset != user.getPreferences().getTimezoneOffset()) {
                Map<String, String> updateData = new HashMap<String, String>();
                updateData.put("preferences.timezoneOffset", String.valueOf(offset));
                mAPIHelper.apiService.updateUser(updateData, new HabitRPGUserCallback(this));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateHeader();
                    updateSidebar();
                    saveLoginInformation();
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
        this.user = user;
        MainActivity.this.setUserData();
    }

    @Override
    public void onUserFail() {
    }

    public void setActiveFragment(BaseFragment fragment) {
        this.activeFragment = fragment;
        this.drawer.setSelectionAtPosition(this.activeFragment.fragmentSidebarPosition, false);
    }

    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkout.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if(checkout != null)
            checkout.stop();

        super.onDestroy();
    }

    public void showSnackbar(String content) {
        showSnackbar(content, SnackbarDisplayType.NORMAL);
    }

    public void showSnackbar(String content, SnackbarDisplayType displayType) {
        Snackbar snackbar = Snackbar.make(floatingMenuWrapper, content, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();

        if (displayType == SnackbarDisplayType.FAILURE) {

            //change Snackbar's background color;
            snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.worse_10));
        } else if (displayType == SnackbarDisplayType.DROP) {
            TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(5);
            snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.best_10));
        }
        snackbar.show();
    }
}
