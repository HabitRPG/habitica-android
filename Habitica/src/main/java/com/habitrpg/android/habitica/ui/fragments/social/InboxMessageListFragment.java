package com.habitrpg.android.habitica.ui.fragments.social;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.commands.SendNewInboxMessageCommand;
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import org.greenrobot.eventbus.Subscribe;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keithholliday on 6/20/16.
 */
public class InboxMessageListFragment extends BaseMainFragment
            implements SwipeRefreshLayout.OnRefreshListener, HabitRPGUserCallback.OnUserReceived {

        @BindView(R.id.inbox_refresh_layout)
        SwipeRefreshLayout swipeRefreshLayout;

        @BindView(R.id.chat_list)
        RecyclerView chatRecyclerView;

        List<ChatMessage> messages;
        ChatRecyclerViewAdapter chatAdapter;
        String chatRoomUser;
        String replyToUserUUID;

        public InboxMessageListFragment() {
            messages = new ArrayList<ChatMessage>();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            View view = inflater.inflate(R.layout.fragment_inbox_message_list, container, false);
            ButterKnife.bind(this, view);
            swipeRefreshLayout.setOnRefreshListener(this);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
            chatRecyclerView.setLayoutManager(layoutManager);

            chatAdapter = new ChatRecyclerViewAdapter(messages, user, null, false);
            chatAdapter.setToInboxChat(this.replyToUserUUID);
            chatAdapter.setSendingUser(this.user);
            chatRecyclerView.setAdapter(chatAdapter);

            return view;
        }

        @Override
        public void injectFragment(AppComponent component) {
            component.inject(this);
        }

        private void refreshUserInbox () {
            this.swipeRefreshLayout.setRefreshing(true);
            this.apiClient.retrieveUser(true)

                    .subscribe(new HabitRPGUserCallback(this), throwable -> {});
        }

        @Override
        public void onRefresh() {
            this.refreshUserInbox();
        }

        public void setMessages(Map<String, ChatMessage> messages, String chatRoomUser, String replyToUserUUID) {
            this.chatRoomUser = chatRoomUser;
            this.replyToUserUUID = replyToUserUUID;

            this.messages = new ArrayList<>();

            for (Object o : messages.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                ChatMessage message = (ChatMessage) pair.getValue();
                if (!message.uuid.equals(replyToUserUUID)) continue;
                this.messages.add(0, message);
            }

            Collections.sort(this.messages,
                    (message1, message2) -> message2.timestamp.compareTo(message1.timestamp));

            if (this.chatAdapter != null) {
                chatAdapter.setToInboxChat(replyToUserUUID);
                this.chatAdapter.setMessages(this.messages);
            }
        }

        @Override
        public void onUserReceived(HabitRPGUser user) {
            this.user = user;
            this.setMessages(user.getInbox().getMessages(), this.chatRoomUser, this.replyToUserUUID);
            swipeRefreshLayout.setRefreshing(false);
        }

        @Subscribe
        public void onEvent(SendNewInboxMessageCommand cmd) {
            HashMap<String, String> messageObject = new HashMap<>();
            messageObject.put("message", cmd.Message);
            messageObject.put("toUserId", cmd.UserToSendTo);

            apiClient.postPrivateMessage(messageObject)

                    .subscribe(postChatMessageResult -> {
                        this.refreshUserInbox();
                    }, throwable -> {
                    });

            UiUtils.dismissKeyboard(HabiticaApplication.currentActivity);
        }


}
