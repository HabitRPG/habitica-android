package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.habitrpg.android.habitica.api.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.helpers.AmplitudeManager;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.fragments.setup.AvatarSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.TaskSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.WelcomeFragment;
import com.habitrpg.android.habitica.ui.views.FadingViewPager;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.IconPagerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;

public class SetupActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    @Inject
    public ApiClient apiClient;
    @Inject
    protected HostConfig hostConfig;
    @Inject
    protected UserRepository userRepository;
    @Inject
    protected TaskRepository taskRepository;
    @BindView(R.id.view_pager)
    FadingViewPager pager;
    @BindView(R.id.nextButton)
    Button nextButton;
    @BindView(R.id.previousButton)
    Button previousButton;
    @BindView(R.id.view_pager_indicator)
    IconPageIndicator indicator;
    AvatarSetupFragment avatarSetupFragment;
    TaskSetupFragment taskSetupFragment;
    @Nullable
    User user;
    boolean completedSetup = false;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeSubscription.add(userRepository.getUser(hostConfig.getUser())
                .flatMap(user -> {
                    if (user == null) {
                        return userRepository.retrieveUser(true);
                    } else {
                        return Observable.just(user);
                    }
                })
                .subscribe(this::onUserReceived, throwable -> {
        }));

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("status", "displayed");
        AmplitudeManager.sendEvent("setup", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData);

        String currentDeviceLanguage = Locale.getDefault().getLanguage();
        for (String language : getResources().getStringArray(R.array.LanguageValues)) {
            if (language.equals(currentDeviceLanguage)) {
                apiClient.registrationLanguage(currentDeviceLanguage)
                        .subscribe(habitRPGUser -> {}, throwable -> {
                        });
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = getWindow().getDecorView();
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_gray_bg));
            } else {
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.days_gray));
            }
        }

        pager.disableFading = true;
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        userRepository.close();
        super.onDestroy();
    }

    private void setupViewpager() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        pager.setAdapter(new ViewPageAdapter(fragmentManager));

        pager.addOnPageChangeListener(this);
        indicator.setViewPager(pager);
    }

    @Subscribe
    public void onEvent(UpdateUserCommand event) {
        this.userRepository.updateUser(user, event.updateData)
                .subscribe(this::onUserReceived, throwable -> {
                });
    }

    @Subscribe
    public void onEvent(EquipCommand event) {
        this.apiClient.equipItem(event.type, event.key)
                .subscribe(items -> {}, throwable -> {
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
            if (this.taskSetupFragment == null) {
                return;
            }
            List<Task> newTasks = this.taskSetupFragment.createSampleTasks();
            this.completedSetup = true;
            this.taskRepository.createTasks(newTasks)
                    .subscribe(tasks -> onUserReceived(user), throwable -> {
                    });
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

    public void onUserReceived(User user) {
        if (completedSetup) {
            if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
                compositeSubscription.unsubscribe();
            }
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
            if (this.taskSetupFragment != null) {
                this.taskSetupFragment.setUser(user);
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
            Fragment fragment;

            switch (position) {
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
                default: {
                    fragment = new WelcomeFragment();
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
        return this.pager == null || this.pager.getAdapter() == null || this.pager.getCurrentItem() == this.pager.getAdapter().getCount() - 1;
    }
}
