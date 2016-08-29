package com.habitrpg.android.habitica.api;

import com.habitrpg.android.habitica.BuildConfig;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

import java.util.List;
import java.util.UUID;

import rx.observers.TestSubscriber;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
@RunWith(RobolectricGradleTestRunner.class)
public class TagAPITests extends BaseAPITests {

    @Test
    public void shouldCreateTag() {
        TestSubscriber<Tag> testSubscriber = new TestSubscriber<>();
        Tag tag = new Tag();
        tag.setName("foo");
        apiHelper.apiService.createTag(tag).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);

    }

    @Test
    public void shouldUpdateTag() {
        TestSubscriber<Tag> testSubscriber = new TestSubscriber<>();
        TestSubscriber<HabitRPGUser> userSubscriber = new TestSubscriber<>();

        Tag t = new Tag();
        String newname = "BAR";
        t.setId(String.valueOf(UUID.randomUUID()));
        t.setName(newname);

        //Get the test user so we can obtain their tags
        apiHelper.apiService.getUser().subscribe(userSubscriber);
        userSubscriber.assertNoErrors();
        userSubscriber.assertCompleted();
        List<HabitRPGUser> users = userSubscriber.getOnNextEvents();

        //Attempt to update their first tag
        String testId = users.get(0).getTags().get(0).getId();
        apiHelper.apiService.updateTag(testId,t).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        Assert.assertEquals(newname,testSubscriber.getOnNextEvents().get(0).getName());

    }

    @Test
    public void shouldDeleteTag() {
        TestSubscriber<Void> testSub = new TestSubscriber<>();
        TestSubscriber<HabitRPGUser> userSubscriber = new TestSubscriber<>();

        apiHelper.apiService.getUser().subscribe(userSubscriber);
        userSubscriber.assertNoErrors();
        userSubscriber.assertCompleted();
        List<HabitRPGUser> users = userSubscriber.getOnNextEvents();

        String testId = users.get(0).getTags().get(0).getId();
        apiHelper.apiService.deleteTag(testId).subscribe(testSub);
        testSub.assertNoErrors();
        testSub.assertCompleted();
    }

}
