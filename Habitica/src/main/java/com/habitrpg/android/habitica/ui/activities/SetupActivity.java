package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.ItemsCallback;
import com.habitrpg.android.habitica.callbacks.MergeUserCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.helpers.AmplitudeManager;
import com.habitrpg.android.habitica.ui.fragments.setup.AvatarSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.IntroFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.TaskSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.WelcomeFragment;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.IconPagerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class SetupActivity extends BaseActivity implements ViewPager.OnPageChangeListener, HabitRPGUserCallback.OnUserReceived {

    @Inject
    public ApiClient apiClient;
    @Inject
    protected HostConfig hostConfig;
    @BindView(R.id.view_pager)
    ViewPager pager;
    @BindView(R.id.nextButton)
    Button nextButton;
    @BindView(R.id.previousButton)
    Button previousButton;
    @BindView(R.id.view_pager_indicator)
    IconPageIndicator indicator;
    AvatarSetupFragment avatarSetupFragment;
    TaskSetupFragment taskSetupFragment;
    HabitRPGUser user;
    Boolean completedSetup;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.user = new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).querySingle();

        this.completedSetup = false;

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("status", "displayed");
        AmplitudeManager.sendEvent("setup", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData);

        String currentDeviceLanguage = Locale.getDefault().getLanguage();
        for (String language : getResources().getStringArray(R.array.LanguageValues)) {
            if (language.equals(currentDeviceLanguage)) {
                apiClient.registrationLanguage(currentDeviceLanguage)
                        .subscribe(new MergeUserCallback(this, user), throwable -> {
                        });
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.days_gray));
        }
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        if (this.pager.getAdapter() == null) {
            if (this.user != null) {
                setupViewpager();
            } else {
                this.apiClient.getUser()
                        .subscribe(new HabitRPGUserCallback(this), throwable -> {
                        });
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void setupViewpager() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        pager.setAdapter(new ViewPageAdapter(fragmentManager));

        pager.addOnPageChangeListener(this);
        indicator.setViewPager(pager);
    }

    @Subscribe
    public void onEvent(UpdateUserCommand event) {
        this.apiClient.updateUser(event.updateData)
                .subscribe(new MergeUserCallback(this, user), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(EquipCommand event) {
        this.apiHelper.apiService.equipItem(event.type, event.key)
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new ItemsCallback(this, this.user), throwable -> {
                });
    }

    @OnClick(R.id.nextButton)
    public void nextClicked(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FirstDayOfTheWeek",
                Integer.toString(Calendar.getInstance().getFirstDayOfWeek()));
        editor.apply();
        if (isLastPage()) {
            List<Task> newTasks = this.taskSetupFragment.createSampleTasks();
            this.completedSetup = true;
            this.apiHelper.apiService.createTasks(newTasks)
                    .compose(this.apiHelper.configureApiCallObserver())
                    .subscribe(tasks -> {
                        onUserReceived(user);
                    }, throwable -> {
                    });
            //this.apiHelper.apiService.batchOperation(operations, new HabitRPGUserCallback(this));
        }
        this.pager.setCurrentItem(this.pager.getCurrentItem() + 1);
    }

    @OnClick(R.id.previousButton)
    public void previousClicked() {
        this.pager.setCurrentItem(this.pager.getCurrentItem() - 1);
    }

    private void setPreviousButtonEnabled(boolean enabled) {
        Drawable leftDrawable;
        if (enabled) {
            previousButton.setText(R.string.action_back);
            leftDrawable = AppCompatResources.getDrawable(this, R.drawable.back_arrow_enabled);
        } else {
            previousButton.setText(null);
            leftDrawable = AppCompatResources.getDrawable(this, R.drawable.back_arrow_disabled);
        }
        previousButton.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            this.setPreviousButtonEnabled(false);
            this.nextButton.setText(this.getString(R.string.next_button));
        } else if (isLastPage()) {
            this.setPreviousButtonEnabled(true);
            this.nextButton.setText(this.getString(R.string.intro_finish_button));
        } else {
            this.setPreviousButtonEnabled(true);
            this.nextButton.setText(this.getString(R.string.next_button));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        if (completedSetup) {
            this.startMainActivity();
            return;
        }
        this.user = user;
        if (this.pager.getAdapter() == null) {
            this.setupViewpager();
        } else {
            if (this.avatarSetupFragment != null) {
                this.avatarSetupFragment.setUser(user);
            }
        }

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("status", "completed");
        AmplitudeManager.sendEvent("setup", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData);
    }

    private void startMainActivity() {
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private class ViewPageAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0: {
                    fragment = new WelcomeFragment();
                    break;
                }
                case 1: {
                    avatarSetupFragment = new AvatarSetupFragment();
                    avatarSetupFragment.activity = SetupActivity.this;
                    avatarSetupFragment.setUser(user);
                    avatarSetupFragment.width = pager.getWidth();
                    fragment = avatarSetupFragment;
                    break;
                }
                case 2: {
                    taskSetupFragment = new TaskSetupFragment();
                    taskSetupFragment.setUser(user);
                    fragment = taskSetupFragment;
                    break;
                }
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public int getIconResId(int index) {
            return R.drawable.indicator_diamond;
        }
    }

    private boolean isLastPage() {
        return this.pager.getCurrentItem() == this.pager.getAdapter().getCount()-1;
    }
}
