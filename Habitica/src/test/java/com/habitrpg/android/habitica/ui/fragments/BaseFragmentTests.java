package com.habitrpg.android.habitica.ui.fragments;


import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.LinearLayout;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
abstract public class BaseFragmentTests<F extends Fragment> {

    public F fragment;
    private ActivityController<FragmentControllerActivity> activityController;
    private FragmentControllerActivity activity;
/*
    @Before
    public void setUp() {
        activityController = Robolectric.buildActivity(FragmentControllerActivity.class);
        Intent intent = new Intent(RuntimeEnvironment.application, FragmentControllerActivity.class);
        activity = activityController
                .withIntent(intent)
                .create()
                .get();
    }

    @Test
    public void fragmentLifecycleTest() throws Exception {
        assertNull(this.fragment.getView());
        this.activity.getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_container, this.fragment).commit();

        this.activityController.start().resume().visible();

        assertNotNull(this.fragment.getView());
    }

    @After
    public void tearDown() {
        this.activityController.pause().stop().destroy();
    }
*/
    private static class FragmentControllerActivity extends MainActivity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LinearLayout view = new LinearLayout(this);
            view.setId(R.id.frame_container);

            setContentView(view);
        }

        @Override
        public void setActiveFragment(BaseMainFragment fragment) {
        }
    }
}
