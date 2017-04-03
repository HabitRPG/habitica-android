package com.habitrpg.android.habitica.api;

import com.habitrpg.android.habitica.BuildConfig;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.observers.TestSubscriber;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
@RunWith(RobolectricTestRunner.class)
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
        TestSubscriber<HabitResponse<PostChatMessageResult>> testSubscriber = new TestSubscriber<>();
        apiClient.postGroupChat(groupID, messageObject).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        PostChatMessageResult result = testSubscriber.getOnNextEvents().get(0).getData();
        messagesIDs.add(result.message.id);
    }

    @Test
    public void shouldLoadTavernWithMessages() {
        groupID = "habitrpg";
        postMessage(groupID, "1");

        TestSubscriber<HabitResponse<Group>> testSubscriber = new TestSubscriber<>();
        apiClient.getGroup(groupID).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldLoadTavernChat() {
        groupID = "habitrpg";
        postMessage(groupID, "1");
        postMessage(groupID, "2");

        TestSubscriber<HabitResponse<List<ChatMessage>>> testSubscriber = new TestSubscriber<>();
        apiClient.listGroupChat(groupID).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
    }

    @After
    public void tearDown() {
        TestSubscriber<HabitResponse<Void>> testSubscriber = new TestSubscriber<>();
        for (String messageID : this.messagesIDs) {
            apiClient.deleteMessage("habitrpg", messageID)
                    .subscribe(testSubscriber);
            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();
        }
        super.tearDown();
    }
}
