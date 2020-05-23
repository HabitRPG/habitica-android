//package com.habitrpg.android.habitica.helpers;
//
//import com.habitrpg.android.habitica.BuildConfig;
//import com.habitrpg.android.habitica.api.HostConfig;
//import com.habitrpg.android.habitica.models.Notification;
//import com.habitrpg.android.habitica.models.notifications.LoginIncentiveData;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.robolectric.RobolectricTestRunner;
//import org.robolectric.RuntimeEnvironment;
//import org.robolectric.annotation.Config;
//import org.robolectric.shadows.ShadowAlertDialog;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.os.Build;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertNull;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
///**
// * Created by krh12 on 12/9/2016.
// */
//
//@Config(sdk = Build.VERSION_CODES.M)
//public class PopupNotificationsManagerTest {
//
//    public HostConfig hostConfig;
//    private Context context;
//    private NotificationsManager notificationsManager;
//
//    @Before
//    public void setUp() {
//        context = mock(Context.class);
//        hostConfig = new HostConfig(BuildConfig.BASE_URL,
//                BuildConfig.PORT,
//                "",
//                "");
//        notificationsManager = new NotificationsManager(context);
//    }

//    @Test
//    public void itDoesNothingWhenNotificationsListIsEmpty() {
//        List<Notification> notifications = new ArrayList<>();
//        notificationsManager.handlePopupNotifications(notifications);
//
//        AlertDialog alert =
//                ShadowAlertDialog.getLatestAlertDialog();
//        assertNull(alert);
//    }

//    @Test
//    // @TODO: Eventually, we should have a list of implemented notifications and only use those
//    public void itShouldNotDisplayNotificationsThatAreNotLoginIncentives() {
//        List<Notification> notifications = new ArrayList<>();
//
//        Notification notification = new Notification();
//        notification.setType("NOT_LOGIN_INCENTIVE");
//
//        notifications.add(notification);
//
//        final NotificationsManager testClass = mock(NotificationsManager.class);
//        Mockito.when(testClass.displayLoginIncentiveNotification(notification)).thenReturn(true);
//        Mockito.when(testClass.handlePopupNotifications(notifications)).thenCallRealMethod();
//
//        testClass.handlePopupNotifications(notifications);
//
//        verify(testClass, times(0)).displayLoginIncentiveNotification(notification);
//    }
//
//    @Test
//    public void itShouldDisplayADialogueForANotification() {
//        String testTitle = "Test Title";
//
//        List<Notification> notifications = new ArrayList<>();
//
//        LoginIncentiveData notificationData = new LoginIncentiveData();
//        notificationData.setMessage(testTitle);
//
//        Notification notification = new Notification();
//        notification.setType("LOGIN_INCENTIVE");
//        notification.setData(notificationData);
//
//        notifications.add(notification);
//
//        final NotificationsManager testClass = mock(NotificationsManager.class);
//        Mockito.when(testClass.displayLoginIncentiveNotification(notification)).thenReturn(true);
//        Mockito.when(testClass.handlePopupNotifications(notifications)).thenCallRealMethod();
//
//        testClass.handlePopupNotifications(notifications);
//
//        verify(testClass, times(1)).displayLoginIncentiveNotification(notification);
//    }
//
//    @Test
//    public void itShouldNotDisplayANotificationTwice() {
//        String testTitle = "Test Title";
//
//        List<Notification> notifications = new ArrayList<>();
//
//        LoginIncentiveData notificationData = new LoginIncentiveData();
//        notificationData.setMessage(testTitle);
//
//        Notification notification = new Notification();
//        notification.setType("LOGIN_INCENTIVE");
//        notification.setData(notificationData);
//
//        notifications.add(notification);
//        notifications.add(notification);
//
//        final NotificationsManager testClass = mock(NotificationsManager.class);
//        Mockito.when(testClass.displayLoginIncentiveNotification(notification)).thenReturn(true);
//        Mockito.when(testClass.handlePopupNotifications(notifications)).thenCallRealMethod();
//
//        testClass.handlePopupNotifications(notifications);
//
//        verify(testClass, times(1)).displayLoginIncentiveNotification(notification);
//    }
//}
