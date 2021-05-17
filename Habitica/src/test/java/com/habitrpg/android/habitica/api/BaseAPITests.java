//package com.habitrpg.android.habitica.api;
//
//
//import com.habitrpg.android.habitica.data.implementation.ApiClientImpl;
//import com.habitrpg.android.habitica.helpers.NotificationsManager;
//import com.habitrpg.android.habitica.proxy.implementation.EmptyAnalyticsManager;
//import com.habitrpg.android.habitica.data.ApiClient;
//import com.habitrpg.android.habitica.BuildConfig;
//import com.habitrpg.android.habitica.models.user.User;
//import com.habitrpg.android.habitica.models.auth.UserAuthResponse;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.robolectric.RuntimeEnvironment;
//import org.robolectric.shadows.ShadowApplication;
//
//import android.content.Context;
//
//import java.security.InvalidParameterException;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//import rx.Scheduler;
//import rx.android.plugins.RxAndroidPlugins;
//import rx.android.schedulers.AndroidSchedulers;
//
//import rx.observers.TestSubscriber;
//import rx.plugins.RxJavaPlugins;
//import rx.plugins.RxJavaSchedulersHook;
//
//import static org.junit.Assert.assertTrue;
//
//import rx.plugins.RxJavaTestPlugins;
//import rx.schedulers.Schedulers;
//
//
//public class BaseAPITests {
//
//    public ApiClient apiClient;
//    public HostConfig hostConfig;
//
//    public String username;
//    public final String password = "password";
//
//    @Before
//    public void setUp() {
//        if (BuildConfig.BASE_URL.contains("habitica.com")) {
//            throw new InvalidParameterException("Can't test against production server.");
//        }
//
//        RxJavaTestPlugins.resetPlugins();
//        RxJavaPlugins.getInstance().registerSchedulersHook(new RxJavaSchedulersHook() {
//            @Override
//            public Scheduler getIOScheduler() {
//                return AndroidSchedulers.mainThread();
//            }
//        });
//
//        Context context = RuntimeEnvironment.application;
//        hostConfig = new HostConfig(BuildConfig.BASE_URL,
//                BuildConfig.PORT,
//                "",
//                "");
//        //apiClient = new ApiClientImpl(ApiClientImpl.createGsonFactory(), hostConfig, new EmptyCrashlyticsProxy(), new NotificationsManager(context), context);
//        //generateUser();
//    }
//
//    public void generateUser() {
//        TestSubscriber<UserAuthResponse> testSubscriber = new TestSubscriber<>();
//        username = UUID.randomUUID().toString();
//        apiClient.registerUser(username, username+"@example.com", password, password)
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS);
//        testSubscriber.assertCompleted();
//        UserAuthResponse response = testSubscriber.getOnNextEvents().get(0);
//        hostConfig.setUser(response.getId());
//        hostConfig.setApi(response.getApiToken() != null ? response.getApiToken() : response.getToken());
//    }
//
//    public User getUser() {
//        TestSubscriber<User> userSubscriber = new TestSubscriber<>();
//
//        apiClient.getUser()
//                .subscribe(userSubscriber);
//        userSubscriber.awaitTerminalEvent();
//        userSubscriber.assertNoErrors();
//        userSubscriber.assertCompleted();
//        User user = userSubscriber.getOnNextEvents().get(0);
//
//        return user;
//    }
//
//    @Test
//    public void emptyTest() {
//        assertTrue(true);
//    }
//
//    @After
//    public void tearDown() {
//    }
//}
