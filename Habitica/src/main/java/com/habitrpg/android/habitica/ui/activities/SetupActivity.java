package com.habitrpg.android.habitica.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.amplitude.api.Amplitude;
import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.ui.fragments.setup.AvatarSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.TaskSetupFragment;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class SetupActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, HabitRPGUserCallback.OnUserReceived {

    @BindView(R.id.view_pager)
    ViewPager pager;

    @BindView(R.id.skipButton)
    Button skipButton;

    @BindView(R.id.nextButton)
    Button nextButton;

    @BindView(R.id.previousButton)
    Button previousButton;

    AvatarSetupFragment avatarSetupFragment;
    TaskSetupFragment taskSetupFragment;

    private APIHelper apiHelper;
    protected HostConfig hostConfig;
    HabitRPGUser user;
    Boolean completedSetup;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.hostConfig = PrefsActivity.fromContext(this);

        this.apiHelper = new APIHelper(hostConfig);

        this.user = new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).querySingle();


        this.skipButton.setOnClickListener(this);
        this.nextButton.setOnClickListener(this);
        this.previousButton.setOnClickListener(this);
        this.completedSetup = false;

        JSONObject eventProperties = new JSONObject();
        try {
            eventProperties.put("eventAction", "setup");
            eventProperties.put("eventCategory", "behaviour");
            eventProperties.put("hitType", "event");
            eventProperties.put("status", "displayed");
        } catch (JSONException exception) {
        }
        Amplitude.getInstance().logEvent("setup", eventProperties);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (this.pager.getAdapter() == null) {
            if (this.user != null) {
                setupViewpager();
            } else {
                this.apiHelper.retrieveUser(new HabitRPGUserCallback(this));
            }
        }
    }

    private void setupViewpager() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        pager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Fragment fragment;

                if (position == 0) {
                    avatarSetupFragment = new AvatarSetupFragment();
                    avatarSetupFragment.activity = SetupActivity.this;
                    avatarSetupFragment.setUser(user);
                    avatarSetupFragment.width = pager.getWidth();
                    fragment = avatarSetupFragment;
                } else {
                    taskSetupFragment = new TaskSetupFragment();
                    fragment = taskSetupFragment;
                }

                return fragment;
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        pager.addOnPageChangeListener(this);
    }

    @Subscribe
    public void onEvent(UpdateUserCommand event) {
        this.apiHelper.apiService.updateUser(event.updateData, new HabitRPGUserCallback(this));
    }

    @Override
    public void onClick(View v) {
        if (v == this.nextButton) {
            if (this.pager.getCurrentItem() == 1) {
                List<Map<String, Object>> operations = this.taskSetupFragment.createSampleTasks();
                this.completedSetup = true;
                this.apiHelper.apiService.batchOperation(operations, new HabitRPGUserCallback(this));
            }
            this.pager.setCurrentItem(this.pager.getCurrentItem()+1);
        } else if (v == this.previousButton) {
            this.pager.setCurrentItem(this.pager.getCurrentItem()-1);
        } else if (v == this.skipButton) {
            JSONObject eventProperties = new JSONObject();
            try {
                eventProperties.put("eventAction", "setup");
                eventProperties.put("eventCategory", "behaviour");
                eventProperties.put("hitType", "event");
                eventProperties.put("status", "skipped");
            } catch (JSONException exception) {
            }
            Amplitude.getInstance().logEvent("setup", eventProperties);
            this.startMainActivity();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            this.previousButton.setVisibility(View.GONE);
            this.nextButton.setText(this.getString(R.string.next_button));
        } else if (position == 1) {
            this.previousButton.setVisibility(View.VISIBLE);
            this.nextButton.setText(this.getString(R.string.intro_finish_button));
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

        JSONObject eventProperties = new JSONObject();
        try {
            eventProperties.put("eventAction", "setup");
            eventProperties.put("eventCategory", "behaviour");
            eventProperties.put("hitType", "event");
            eventProperties.put("status", "completed");
        } catch (JSONException exception) {
        }
        Amplitude.getInstance().logEvent("setup", eventProperties);
    }

    @Override
    public void onUserFail() {

    }

    private void startMainActivity() {
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
