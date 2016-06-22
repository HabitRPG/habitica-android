package com.habitrpg.android.habitica.ui.fragments.social;

import com.facebook.internal.BoltsMeasurementEventListener;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InboxFragment extends BaseMainFragment
        implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, HabitRPGUserCallback.OnUserReceived {

    @BindView(R.id.inbox_messages)
    LinearLayout inboxMessagesListView;

    @BindView(R.id.inbox_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    Map<String, ChatMessage> messages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_inbox, container, false);
        unbinder = ButterKnife.bind(this, v);

        swipeRefreshLayout.setOnRefreshListener(this);

        this.messages = this.user.getInbox().getMessages();
        if (this.messages != null) {
            this.setInboxMessages();
        }

        return v;
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

    public void setInboxMessages() {
        if (this.inboxMessagesListView == null) {
            return;
        }

        this.inboxMessagesListView.removeAllViewsInLayout();

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Map<String,Boolean> roomsAdded = new HashMap<String, Boolean>();

        Iterator it = this.messages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            ChatMessage message = (ChatMessage) pair.getValue();
            if (roomsAdded.get(message.user) != null && roomsAdded.get(message.user)) return;
            roomsAdded.put(message.user, true);

            TextView entry = (TextView) inflater.inflate(R.layout.plain_list_item, this.inboxMessagesListView, false);
            entry.setText(message.user);
            entry.setOnClickListener(this);
            this.inboxMessagesListView.addView(entry);
        }

    }

    @Override
    public void onClick(View v) {
        TextView entry = (TextView) v;
        InboxMessageListFragment inboxMessageListFragment = new InboxMessageListFragment();
        inboxMessageListFragment.setMessages(this.messages, entry.getText().toString());
        this.activity.displayFragment(inboxMessageListFragment);
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        this.user = user;
        this.messages = user.getInbox().getMessages();
        this.setInboxMessages();
        swipeRefreshLayout.setRefreshing(false);
    }
}
