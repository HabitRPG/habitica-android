package com.habitrpg.android.habitica.api;

import com.habitrpg.android.habitica.BuildConfig;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

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

}
