package com.habitrpg.android.habitica.api;

import com.habitrpg.android.habitica.BuildConfig;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
public class SocialAPITests extends BaseAPITests {

    List<String> messagesIDs;
    String groupID;

    @Override
    public void setUp() {
        super.setUp();
        groupID = null;
        messagesIDs = new ArrayList<>();
    }

    public void postMessage(String groupID, String messageSuffix) {
        HashMap<String, String> messageObject = new HashMap<>();
        messageObject.put("message", "Foo Bar"+messageSuffix);
        TestSubscriber<PostChatMessageResult> testSubscriber = new TestSubscriber<>();
        apiHelper.apiService.postGroupChat(groupID, messageObject).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        PostChatMessageResult result = testSubscriber.getOnNextEvents().get(0);
        messagesIDs.add(result.message.id);
    }

    @Test
    public void shouldPostMessageToTavern() {
        groupID = "habitrpg";
        HashMap<String, String> messageObject = new HashMap<>();
        messageObject.put("message", "Foo Bar");
        TestSubscriber<PostChatMessageResult> testSubscriber = new TestSubscriber<>();
        apiHelper.apiService.postGroupChat(groupID, messageObject).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        PostChatMessageResult result = testSubscriber.getOnNextEvents().get(0);
        messagesIDs.add(result.message.id);
        assertEquals("Foo Bar", result.message.text);
    }

    @Test
    public void shouldLoadTavernWithMessages() {
        groupID = "habitrpg";
        postMessage(groupID, "1");

        TestSubscriber<Group> testSubscriber = new TestSubscriber<>();
        apiHelper.apiService.getGroup(groupID).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        Group group = testSubscriber.getOnNextEvents().get(0);
        assertEquals(1, group.chat.size());
    }

    @Test
    public void shouldLoadTavernChat() {
        groupID = "habitrpg";
        postMessage(groupID, "1");
        postMessage(groupID, "2");

        TestSubscriber<List<ChatMessage>> testSubscriber = new TestSubscriber<>();
        apiHelper.apiService.listGroupChat(groupID).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        List<ChatMessage> messages = testSubscriber.getOnNextEvents().get(0);
        assertEquals(2, messages.size());
    }

    @After
    public void tearDown() {
        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        for (String messageID : this.messagesIDs) {
            apiHelper.apiService.deleteMessage("habitrpg", messageID)
                    .subscribe(testSubscriber);
            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();
        }
        super.tearDown();
    }
}
