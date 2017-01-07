package com.habitrpg.android.habitica.api;

import com.habitrpg.android.habitica.BuildConfig;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

import java.util.UUID;

import rx.observers.TestSubscriber;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
@RunWith(RobolectricGradleTestRunner.class)
public class TagAPITests extends BaseAPITests {

    @Test
    public void shouldCreateTag() {
        TestSubscriber<HabitResponse<Tag>> testSubscriber = new TestSubscriber<>();
        Tag tag = new Tag();
        tag.setName("foo");
        apiHelper.apiService.createTag(tag).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);

    }

    @Test
    public void shouldUpdateTag() {
        TestSubscriber<HabitResponse<Tag>> testSubscriber = new TestSubscriber<>();

        Tag t = new Tag();
        String newname = "BAR";
        t.setId(String.valueOf(UUID.randomUUID()));
        t.setName(newname);

        //Attempt to update the test user's first tag
        String testId = getUser().getTags().get(0).getId();
        apiHelper.apiService.updateTag(testId,t).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        Assert.assertEquals(newname,testSubscriber.getOnNextEvents().get(0).getData().getName());

    }

    @Test
    public void shouldDeleteTag() {
        TestSubscriber<HabitResponse<Void>> testSub = new TestSubscriber<>();

        String testId = getUser().getTags().get(0).getId();
        apiHelper.apiService.deleteTag(testId).subscribe(testSub);
        testSub.assertNoErrors();
        testSub.assertCompleted();
    }

}
