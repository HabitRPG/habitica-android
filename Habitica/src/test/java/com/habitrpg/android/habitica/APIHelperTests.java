package com.habitrpg.android.habitica;


import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

import java.util.List;

import rx.observers.TestSubscriber;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class APIHelperTests {

    private APIHelper apiHelper;

    @Before
    public void setUp() {
        HostConfig hostConfig = new HostConfig(BuildConfig.BASE_URL,
                BuildConfig.PORT,
                BuildConfig.TEST_USER_KEY,
                BuildConfig.TEST_USER_ID);
        apiHelper = new APIHelper(APIHelper.createGsonFactory(), hostConfig);
    }

    @Test
    public void shouldLoadUserFromServer() {
        TestSubscriber<HabitRPGUser> testSubscriber = new TestSubscriber<>();
        apiHelper.apiService.getUser().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldLoadTasksFromServer() {
        TestSubscriber<TaskList> testSubscriber = new TestSubscriber<>();
        apiHelper.apiService.getTasks().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
    }

    @Test
    public void shouldLoadCompleteUserFromServer() {
        TestSubscriber<HabitRPGUser> testSubscriber = new TestSubscriber<>();
        apiHelper.retrieveUser(true).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
    }
}
