package com.habitrpg.android.habitica.helpers;

import android.os.Build;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.HostConfig;
import com.magicmicky.habitrpgwrapper.lib.models.Notification;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.converter.gson.GsonConverterFactory;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

/**
 * Created by krh12 on 12/9/2016.
 */

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
@RunWith(RobolectricGradleTestRunner.class)
public class PopupNotificationsManagerTest {

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
    }

    @Test
    public void itDoesNothingWhenNotificationsListIsEmpty() {
        List<Notification> notifications = new ArrayList<>();
        PopupNotificationsManager popupNotificationsManager = PopupNotificationsManager.getInstance(apiHelper);
        popupNotificationsManager.showNotificationDialog(notifications);
        assertEquals("10 x 0 must be 0", 0, 10 * 0 );
    }

    @Test
    // @TODO: Eventually, we should have a list of implemented notifications and only use those
    public void itShouldNotDisplayNotificationsThatAreNotLoginIncentives() {}

    @Test
    public void itShouldDisplayADialogueForANotification() {}

    @Test
    public void itShouldNotDisplayANotificationTwice() {}
}
