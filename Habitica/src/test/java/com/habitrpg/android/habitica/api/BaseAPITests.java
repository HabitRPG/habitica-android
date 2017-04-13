package com.habitrpg.android.habitica.api;


import com.habitrpg.android.habitica.data.implementation.ApiClientImpl;
import com.habitrpg.android.habitica.proxy.impl.EmptyCrashlyticsProxy;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.models.HabitRPGUser;
import com.habitrpg.android.habitica.models.UserAuthResponse;

import org.junit.After;
import org.junit.Before;
import org.robolectric.shadows.ShadowApplication;

import android.content.Context;

import java.security.InvalidParameterException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.observers.TestSubscriber;

public class BaseAPITests {

    public ApiClient apiClient;
    public HostConfig hostConfig;

    public String username;
    public final String password = "password";

    @Before
    public void setUp() {
        if (BuildConfig.BASE_URL.contains("habitica.com")) {
            throw new InvalidParameterException("Can't test against production server.");
        }
        Context context = ShadowApplication.getInstance().getApplicationContext();
        hostConfig = new HostConfig(BuildConfig.BASE_URL,
                BuildConfig.PORT,
                "",
                "");
        apiClient = new ApiClientImpl(ApiClientImpl.createGsonFactory(), hostConfig, new EmptyCrashlyticsProxy(), context);
        generateUser();
    }

    public void generateUser() {
        TestSubscriber<UserAuthResponse> testSubscriber = new TestSubscriber<>();
        username = UUID.randomUUID().toString();
        apiClient.registerUser(username, username+"@example.com", password, password)
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS);
        testSubscriber.assertCompleted();
        UserAuthResponse response = testSubscriber.getOnNextEvents().get(0);
        hostConfig.setUser(response.getId());
        hostConfig.setApi(response.getApiToken() != null ? response.getApiToken() : response.getToken());
    }

    public HabitRPGUser getUser() {
        TestSubscriber<HabitRPGUser> userSubscriber = new TestSubscriber<>();

        apiClient.getUser()
                .subscribe(userSubscriber);
        userSubscriber.awaitTerminalEvent();
        userSubscriber.assertNoErrors();
        userSubscriber.assertCompleted();
        HabitRPGUser user = userSubscriber.getOnNextEvents().get(0);

        return user;
    }

    @After
    public void tearDown() {
    }
}
