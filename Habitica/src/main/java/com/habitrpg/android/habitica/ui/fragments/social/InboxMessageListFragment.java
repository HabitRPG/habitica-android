package com.habitrpg.android.habitica.ui.fragments.social;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.commands.SendNewInboxMessageCommand;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;
import io.realm.Sort;

public class InboxMessageListFragment extends BaseMainFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    SocialRepository socialRepository;
    @Inject
    UserRepository userRepository;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView chatRecyclerView;

    ChatRecyclerViewAdapter chatAdapter;
    String chatRoomUser;
    String replyToUserUUID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_refresh_recyclerview, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        chatRecyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatRecyclerViewAdapter(null, user, null);
        chatAdapter.setToInboxChat(this.replyToUserUUID);
        chatAdapter.setSendingUser(this.user);
        chatRecyclerView.setAdapter(chatAdapter);

        loadMessages();

        return view;
    }

    private void loadMessages() {
        if (user != null && user.isManaged()) {
            user.getInbox().getMessages().where()
                    .equalTo("uuid", replyToUserUUID)
                    .findAllSortedAsync("timestamp", Sort.DESCENDING)
                    .asObservable()
                    .filter(RealmResults::isLoaded)
                    .subscribe(chatMessages -> this.chatAdapter.setMessages(chatMessages), RxErrorHandler.handleEmptyError());
        }
    }

    @Override
    public void onDestroy() {
        socialRepository.close();
        userRepository.close();
        super.onDestroy();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    private void refreshUserInbox () {
        this.swipeRefreshLayout.setRefreshing(true);
        this.userRepository.retrieveUser(true)
                .subscribe(this::onUserReceived, RxErrorHandler.handleEmptyError());
    }

    @Override
    public void onRefresh() {
        this.refreshUserInbox();
    }

    public void onUserReceived(User user) {
        this.user = user;
        swipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe
    public void onEvent(SendNewInboxMessageCommand cmd) {
        socialRepository.postPrivateMessage(cmd.userToSendTo, cmd.message)
                .subscribe(postChatMessageResult -> this.refreshUserInbox(), throwable -> {
                });
        UiUtils.dismissKeyboard(getActivity());
    }


    public void setReceivingUser(String chatRoomUser, String replyToUserUUID) {
        this.chatRoomUser = chatRoomUser;
        this.replyToUserUUID = replyToUserUUID;
    }
}
