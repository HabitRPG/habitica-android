package com.habitrpg.android.habitica.api;


import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.HostConfig;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

import java.util.List;
import java.util.UUID;

import rx.observers.TestSubscriber;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class BaseAPITests {

    public APIHelper apiHelper;
    public HostConfig hostConfig;

    public String username;
    public final String password = "password";

    @Before
    public void setUp() {
        hostConfig = new HostConfig(BuildConfig.BASE_URL,
                BuildConfig.PORT,
                "",
                "");
        apiHelper = new APIHelper(APIHelper.createGsonFactory(), hostConfig);
        generateUser();
    }

    public void generateUser() {
        TestSubscriber<UserAuthResponse> testSubscriber = new TestSubscriber<>();
        username = UUID.randomUUID().toString();
        apiHelper.registerUser(username, username+"@example.com", password, password)
        .subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        UserAuthResponse response = testSubscriber.getOnNextEvents().get(0);
        hostConfig.setUser(response.getId());
        hostConfig.setApi(response.getApiToken() != null ? response.getApiToken() : response.getToken());
    }
}
