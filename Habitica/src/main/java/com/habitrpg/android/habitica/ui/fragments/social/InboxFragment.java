package com.habitrpg.android.habitica.ui.fragments.social;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import org.w3c.dom.Text;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
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
    List<String> roomsAdded;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        this.apiHelper.apiService.markPrivateMessagesRead()
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(aVoid -> {}, throwable -> {});

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

        roomsAdded = new ArrayList<>();

        if (this.messages.size() > 0) {
            for (Object o : this.messages.entrySet()) {
                Map.Entry pair = (Map.Entry) o;

                ChatMessage message = (ChatMessage) pair.getValue();
                if (roomsAdded.contains(message.uuid)) {
                    TextView entry = (TextView) this.inboxMessagesListView.findViewWithTag(message.uuid);
                    entry.setText(message.user);
                } else {
                    roomsAdded.add(message.uuid);

                    TextView entry = (TextView) inflater.inflate(R.layout.plain_list_item, this.inboxMessagesListView, false);
                    entry.setText(message.user);
                    entry.setTag(message.uuid);
                    entry.setOnClickListener(this);
                    this.inboxMessagesListView.addView(entry);
                }
            }
        } else {
            TextView tv = new TextView(getContext());
            tv.setText(R.string.empty_inbox);
        }
    }

    @Override
    public void onClick(View v) {
        TextView entry = (TextView) v;
        InboxMessageListFragment inboxMessageListFragment = new InboxMessageListFragment();
        String replyToUserName = entry.getText().toString();
        inboxMessageListFragment.setMessages(this.messages, replyToUserName, entry.getTag().toString());
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
