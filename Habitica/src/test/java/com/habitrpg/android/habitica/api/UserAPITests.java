package com.habitrpg.android.habitica.api;


import com.habitrpg.android.habitica.BuildConfig;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

import java.util.UUID;

import rx.android.schedulers.AndroidSchedulers;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
@RunWith(RobolectricTestRunner.class)
public class UserAPITests extends BaseAPITests {

    @Test
    public void shouldLoadUserFromServer() {
        TestSubscriber<HabitRPGUser> testSubscriber = new TestSubscriber<>();
        apiClient.getUser()
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldLoadCompleteUserFromServer() {
        TestSubscriber<HabitRPGUser> testSubscriber = new TestSubscriber<>();
        apiClient.retrieveUser(true)
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldRegisterNewUser() {
        hostConfig.setUser("");
        hostConfig.setApi("");
        TestSubscriber<UserAuthResponse> testSubscriber = new TestSubscriber<>();
        username = UUID.randomUUID().toString();
        apiClient.registerUser(username, username+"@example.com", password, password)
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        UserAuthResponse response = testSubscriber.getOnNextEvents().get(0);

        assertNotSame(hostConfig.getUser(), response.getId());
        assertNotSame(hostConfig.getApi(), response.getApiToken() != null ? response.getApiToken() : response.getToken());
    }

    @Test
    public void shouldLoginExistingUser() {
        TestSubscriber<UserAuthResponse> testSubscriber = new TestSubscriber<>();
        apiClient.connectUser(username, password)
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        UserAuthResponse response = testSubscriber.getOnNextEvents().get(0);
        assertEquals(hostConfig.getUser(), response.getId());
        assertEquals(hostConfig.getApi(), response.getApiToken() != null ? response.getApiToken() : response.getToken());
    }

}
