package com.habitrpg.android.habitica.api;


import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.HostConfig;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;

import org.junit.After;
import org.junit.Before;

import java.security.InvalidParameterException;
import java.util.UUID;

import rx.observers.TestSubscriber;

public class BaseAPITests {

    public APIHelper apiHelper;
    public HostConfig hostConfig;

    public String username;
    public final String password = "password";

    @Before
    public void setUp() {
        if (BuildConfig.BASE_URL.contains("habitica.com")) {
            throw new InvalidParameterException("Can't test against production server.");
        }
        hostConfig = new HostConfig(BuildConfig.BASE_URL,
                BuildConfig.PORT,
                "",
                "");
        apiHelper = new APIHelper(APIHelper.createGsonFactory(), hostConfig);
        generateUser();
    }

    public void generateUser() {
        TestSubscriber<HabitResponse<UserAuthResponse>> testSubscriber = new TestSubscriber<>();
        username = UUID.randomUUID().toString();
        apiHelper.registerUser(username, username+"@example.com", password, password)
        .subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        UserAuthResponse response = testSubscriber.getOnNextEvents().get(0).getData();
        hostConfig.setUser(response.getId());
        hostConfig.setApi(response.getApiToken() != null ? response.getApiToken() : response.getToken());
    }

    public HabitRPGUser getUser() {
        TestSubscriber<HabitResponse<HabitRPGUser>> userSubscriber = new TestSubscriber<>();

        apiHelper.apiService.getUser().subscribe(userSubscriber);
        userSubscriber.assertNoErrors();
        userSubscriber.assertCompleted();
        HabitRPGUser user = userSubscriber.getOnNextEvents().get(0).getData();

        return user;
    }

    @After
    public void tearDown() {
    }
}
