package com.habitrpg.android.habitica.ui.fragments.social;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import java.util.ArrayList;
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

            chatAdapter = new ChatRecyclerViewAdapter(messages, null, null, false);
            chatRecyclerView.setAdapter(chatAdapter);

            return view;
        }

        @Override
        public void injectFragment(AppComponent component) {
            component.inject(this);
        }

        @Override
        public void onRefresh() {
            swipeRefreshLayout.setRefreshing(true);
            this.apiHelper.retrieveUser(true)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new HabitRPGUserCallback(this), throwable -> {});
        }

        public void setMessages(Map<String, ChatMessage> messages, String chatRoomUser) {
            this.chatRoomUser = chatRoomUser;

            Iterator it = messages.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                ChatMessage message = (ChatMessage) pair.getValue();
                if (!message.user.equals(chatRoomUser)) continue;
                this.messages.add(message);
            }

            if (this.chatAdapter != null) {
                this.chatAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onUserReceived(HabitRPGUser user) {
            this.user = user;
            this.setMessages(user.getInbox().getMessages(), this.chatRoomUser);
            swipeRefreshLayout.setRefreshing(false);
        }
}
