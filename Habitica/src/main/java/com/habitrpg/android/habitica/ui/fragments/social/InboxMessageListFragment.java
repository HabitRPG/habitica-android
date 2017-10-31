package com.habitrpg.android.habitica.ui.fragments.social;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.commands.SendNewInboxMessageCommand;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity;
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.realm.RealmResults;
import io.realm.Sort;

public class InboxMessageListFragment extends BaseMainFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    SocialRepository socialRepository;
    @Inject
    UserRepository userRepository;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.emoji_button)
    ImageButton emojiButton;
    @BindView(R.id.chat_edit_text)
    EditText chatEditText;
    @BindView(R.id.send_button)
    ImageButton sendButton;
    @BindView(R.id.community_guidelines_view)
    TextView communityGuidelinesView;

    ChatRecyclerViewAdapter chatAdapter;
    String chatRoomUser;
    String replyToUserUUID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        hideToolbar();
        disableToolbarScrolling();
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_inbox_message_list, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatRecyclerViewAdapter(null, true, user, false);
        chatAdapter.setSendingUser(this.user);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setItemAnimator(new SafeDefaultItemAnimator());
        compositeSubscription.add(chatAdapter.getUserLabelClickEvents().subscribe(userId -> FullProfileActivity.open(getContext(), userId), RxErrorHandler.handleEmptyError()));

        loadMessages();

        communityGuidelinesView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.invalidate();
        view.forceLayout();
    }

    private void loadMessages() {
        if (user != null && user.isManaged()) {
            userRepository.getInboxMessages(replyToUserUUID)
                    .first()
                    .subscribe(chatMessages -> this.chatAdapter.updateData(chatMessages), RxErrorHandler.handleEmptyError());
        }
    }

    @Override
    public void onDestroyView() {
        showToolbar();
        enableToolbarScrolling();
        super.onDestroyView();
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
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Subscribe
    public void onEvent(SendNewInboxMessageCommand cmd) {
        socialRepository.postPrivateMessage(cmd.userToSendTo, cmd.message)
                .subscribe(postChatMessageResult -> this.refreshUserInbox(), RxErrorHandler.handleEmptyError());
        UiUtils.dismissKeyboard(getActivity());
    }

    public void setReceivingUser(String chatRoomUser, String replyToUserUUID) {
        this.chatRoomUser = chatRoomUser;
        this.replyToUserUUID = replyToUserUUID;
    }

    @OnTextChanged(R.id.chat_edit_text)
    public void onChatMessageTextChanged() {
        Editable chatText = chatEditText.getText();
        setSendButtonEnabled(chatText.length() > 0);
    }

    private void setSendButtonEnabled(boolean enabled) {
        int tintColor;
        if (enabled) {
            tintColor = ContextCompat.getColor(getContext(), R.color.brand_400);
        } else {
            tintColor = ContextCompat.getColor(getContext(), R.color.md_grey_400);
        }
        sendButton.setEnabled(enabled);
        sendButton.setColorFilter(tintColor);
    }

    @OnClick(R.id.send_button)
    public void sendChatMessage() {
        String chatText = chatEditText.getText().toString();
        if (chatText.length() > 0) {
            chatEditText.setText(null);
            socialRepository.postPrivateMessage(replyToUserUUID, chatText).subscribe(postChatMessageResult -> {
                if (recyclerView != null) {
                    recyclerView.scrollToPosition(0);
                }
            }, RxErrorHandler.handleEmptyError());
        }
    }

    @OnClick(R.id.emoji_button)
    public void openEmojiView() {

    }
}
