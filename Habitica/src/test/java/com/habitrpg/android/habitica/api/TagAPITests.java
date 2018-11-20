//package com.habitrpg.android.habitica.api;
//
//import com.habitrpg.android.habitica.BuildConfig;
//import com.habitrpg.android.habitica.models.Tag;
//
//import junit.framework.Assert;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.RobolectricTestRunner;
//import org.robolectric.annotation.Config;
//
//import android.os.Build;
//
//import java.util.UUID;
//
//import rx.observers.TestSubscriber;
//
//@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
//@RunWith(RobolectricTestRunner.class)
//public class TagAPITests extends BaseAPITests {
//
//    /*@Test
//    public void shouldCreateTag() {
//        TestSubscriber<Tag> testSubscriber = new TestSubscriber<>();
//        Tag tag = new Tag();
//        tag.setName("foo");
//        apiClient.createTag(tag)
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent();
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();
//        testSubscriber.assertValueCount(1);
//
//    }
//
//    @Test
//    public void shouldUpdateTag() {
//        TestSubscriber<Tag> testSubscriber = new TestSubscriber<>();
//
//        Tag t = new Tag();
//        String newname = "BAR";
//        t.setId(String.valueOf(UUID.randomUUID()));
//        t.setName(newname);
//
//        //Attempt to update the test user's first tag
//        String testId = getUserData().getTags().get(0).getId();
//        apiClient.updateTag(testId,t)
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent();
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();
//        Assert.assertEquals(newname,testSubscriber.getOnNextEvents().get(0).getName());
//
//    }
//
//    @Test
//    public void shouldDeleteTag() {
//        TestSubscriber<Void> testSub = new TestSubscriber<>();
//
//        String testId = getUserData().getTags().get(0).getId();
//        apiClient.deleteTag(testId)
//                .subscribe(testSub);
//        testSub.awaitTerminalEvent();
//        testSub.assertNoErrors();
//        testSub.assertCompleted();
//    }*/
//
//}
