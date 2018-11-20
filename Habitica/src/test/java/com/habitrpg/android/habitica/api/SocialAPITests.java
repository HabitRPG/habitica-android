//package com.habitrpg.android.habitica.api;
//
//import com.habitrpg.android.habitica.BuildConfig;
//import com.habitrpg.android.habitica.models.social.ChatMessage;
//import com.habitrpg.android.habitica.models.social.Group;
//import com.habitrpg.android.habitica.models.responses.PostChatMessageResult;
//
//import org.junit.After;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.RobolectricTestRunner;
//import org.robolectric.annotation.Config;
//
//import android.os.Build;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import rx.observers.TestSubscriber;
//
//@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
//@RunWith(RobolectricTestRunner.class)
//public class SocialAPITests extends BaseAPITests {
//
//    List<String> messagesIDs;
//    String groupID;
//
//    @Override
//    public void setUp() {
//        super.setUp();
//        groupID = null;
//        messagesIDs = new ArrayList<>();
//    }
//
//    public void postMessage(String groupID, String messageSuffix) {
//        HashMap<String, String> messageObject = new HashMap<>();
//        messageObject.put("message", "Foo Bar"+messageSuffix);
//        TestSubscriber<PostChatMessageResult> testSubscriber = new TestSubscriber<>();
//        apiClient.postGroupChat(groupID, messageObject)
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS);
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();
//        PostChatMessageResult result = testSubscriber.getOnNextEvents().get(0);
//        messagesIDs.add(result.message.getId());
//    }
//
//    /*@Test
//    public void shouldLoadTavernWithMessages() {
//        groupID = "habitrpg";
//        postMessage(groupID, "1");
//
//        TestSubscriber<Group> testSubscriber = new TestSubscriber<>();
//        apiClient.getGroupData(groupID)
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS);
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();
//        testSubscriber.assertValueCount(1);
//    }
//
//    @Test
//    public void shouldLoadTavernChat() {
//        groupID = "habitrpg";
//        postMessage(groupID, "1");
//        postMessage(groupID, "2");
//
//        TestSubscriber<List<ChatMessage>> testSubscriber = new TestSubscriber<>();
//        apiClient.listGroupChat(groupID)
//                .subscribe(testSubscriber);
//        testSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS);
//        testSubscriber.assertNoErrors();
//        testSubscriber.assertCompleted();
//        testSubscriber.assertValueCount(1);
//    }
//
//    @After
//    public void tearDown() {
//        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
//        for (String messageID : this.messagesIDs) {
//            apiClient.deleteMessage("habitrpg", messageID)
//                    .subscribe(testSubscriber);
//            testSubscriber.awaitTerminalEvent();
//            testSubscriber.assertNoErrors();
//            testSubscriber.assertCompleted();
//        }
//        super.tearDown();
//    }*/
//}
