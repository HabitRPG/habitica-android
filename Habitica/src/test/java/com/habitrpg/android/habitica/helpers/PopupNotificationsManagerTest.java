package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.data.implementation.ApiClientImpl;
import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.proxy.impl.EmptyCrashlyticsProxy;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.models.Notification;
import com.habitrpg.android.habitica.models.notifications.NotificationData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by krh12 on 12/9/2016.
 */

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
@RunWith(RobolectricTestRunner.class)
public class PopupNotificationsManagerTest {

    public ApiClient apiClient;
    public HostConfig hostConfig;

    public String username;
    public final String password = "password";
    private Context context;
    private PopupNotificationsManager popupNotificationsManager;

    @Before
    public void setUp() {
        context = ShadowApplication.getInstance().getApplicationContext();
        hostConfig = new HostConfig(BuildConfig.BASE_URL,
                BuildConfig.PORT,
                "",
                "");
        popupNotificationsManager =new PopupNotificationsManager(context);

        apiClient = new ApiClientImpl(ApiClientImpl.createGsonFactory(), hostConfig, new EmptyCrashlyticsProxy(), popupNotificationsManager, context);
    }

    @Test
    public void itDoesNothingWhenNotificationsListIsEmpty() {
        List<Notification> notifications = new ArrayList<>();
        popupNotificationsManager.showNotificationDialog(notifications);

        AlertDialog alert =
                ShadowAlertDialog.getLatestAlertDialog();
        assertNull(alert);
    }

    @Test
    // @TODO: Eventually, we should have a list of implemented notifications and only use those
    public void itShouldNotDisplayNotificationsThatAreNotLoginIncentives() {
        Activity activity;
        activity = Robolectric.buildActivity(Activity.class).create().get();
        HabiticaApplication.currentActivity = activity;

        List<Notification> notifications = new ArrayList<>();

        Notification notification = new Notification();
        notification.setType("NOT_LOGIN_INCENTIVE");

        notifications.add(notification);

        final PopupNotificationsManager testClass = Mockito.mock(PopupNotificationsManager.class);
        Mockito.when(testClass.displayNotification(notification)).thenReturn(true);
        Mockito.when(testClass.showNotificationDialog(notifications)).thenCallRealMethod();

        testClass.showNotificationDialog(notifications);

        verify(testClass, times(0)).displayNotification(notification);
    }

    @Test
    public void itShouldDisplayADialogueForANotification() {
        Activity activity;
        activity = Robolectric.buildActivity(Activity.class).create().get();
        HabiticaApplication.currentActivity = activity;

        String testTitle = "Test Title";

        List<Notification> notifications = new ArrayList<>();

        NotificationData notificationData = new NotificationData();
        notificationData.message = testTitle;

        Notification notification = new Notification();
        notification.setType("LOGIN_INCENTIVE");
        notification.data = notificationData;

        notifications.add(notification);

        final PopupNotificationsManager testClass = Mockito.mock(PopupNotificationsManager.class);
        Mockito.when(testClass.displayNotification(notification)).thenReturn(true);
        Mockito.when(testClass.showNotificationDialog(notifications)).thenCallRealMethod();

        testClass.showNotificationDialog(notifications);

        verify(testClass, times(1)).displayNotification(notification);
    }

    @Test
    public void itShouldNotDisplayANotificationTwice() {
        Activity activity;
        activity = Robolectric.buildActivity(Activity.class).create().get();
        HabiticaApplication.currentActivity = activity;

        String testTitle = "Test Title";

        List<Notification> notifications = new ArrayList<>();

        NotificationData notificationData = new NotificationData();
        notificationData.message = testTitle;

        Notification notification = new Notification();
        notification.setType("LOGIN_INCENTIVE");
        notification.data = notificationData;

        notifications.add(notification);
        notifications.add(notification);

        final PopupNotificationsManager testClass = Mockito.mock(PopupNotificationsManager.class);
        Mockito.when(testClass.displayNotification(notification)).thenReturn(true);
        Mockito.when(testClass.showNotificationDialog(notifications)).thenCallRealMethod();

        testClass.showNotificationDialog(notifications);

        verify(testClass, times(1)).displayNotification(notification);
    }
}
