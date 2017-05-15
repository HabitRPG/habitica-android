package com.habitrpg.android.habitica.ui.fragments.social;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.CopyChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.DeleteChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.FlagChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.SendNewGroupMessageCommand;
import com.habitrpg.android.habitica.events.commands.ToggleInnCommand;
import com.habitrpg.android.habitica.events.commands.ToggleLikeMessageCommand;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import io.realm.RealmResults;

public class ChatListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public String seenGroupId;
    @Inject
    SocialRepository socialRepository;
    @Inject
    UserRepository userRepository;
    public boolean isTavern;
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
    LinearLayoutManager layoutManager;
    private String groupId;
    private User user;
    private String userId;
    private ChatRecyclerViewAdapter chatAdapter;
    private View view;
    private boolean navigatedOnceToFragment = false;
    private boolean gotNewMessages = false;

    public void configure(String groupId, @Nullable User user, boolean isTavern) {
        this.groupId = groupId;
        this.user = user;
        if (this.user != null) {
            this.userId = this.user.getId();
        }
        this.isTavern = isTavern;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("groupId")) {
                this.groupId = savedInstanceState.getString("groupId");
            }

            if (savedInstanceState.containsKey("isTavern")) {
                this.isTavern = savedInstanceState.getBoolean("isTavern");
            }

            if (savedInstanceState.containsKey("userId")) {
                this.userId = savedInstanceState.getString("userId");
                if (this.userId != null) {
                    userRepository.getUser(userId).subscribe(habitRPGUser -> this.user = habitRPGUser, RxErrorHandler.handleEmptyError());
                }
            }

        }

        if (view == null)
            view = inflater.inflate(R.layout.fragment_chat, container, false);

        return view;
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

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);

        layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(false);

            recyclerView.setLayoutManager(layoutManager);
        }

        chatAdapter = new ChatRecyclerViewAdapter(null, true, user, groupId);

        recyclerView.setAdapter(chatAdapter);

        socialRepository.getGroupChat(groupId).first().subscribe(this::setChatMessages, throwable -> {});
        onRefresh();
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);

        socialRepository.retrieveGroupChat(groupId).subscribe(chatMessages -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, throwable -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void setNavigatedToFragment(String groupId) {
        seenGroupId = groupId;
        navigatedOnceToFragment = true;

        markMessagesAsSeen();
    }

    private void markMessagesAsSeen() {
        if (!isTavern && seenGroupId != null && !seenGroupId.isEmpty()
                && gotNewMessages && navigatedOnceToFragment) {

            gotNewMessages = false;

            socialRepository.markMessagesSeen(seenGroupId);
        }
    }

    @Subscribe
    public void onEvent(CopyChatMessageCommand cmd) {
        ClipboardManager clipMan = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData chatMessage = ClipData.newPlainText("Chat message", cmd.chatMessage.text);
        clipMan.setPrimaryClip(chatMessage);
        MainActivity activity = (MainActivity) getActivity();
        UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), getString(R.string.chat_message_copied), UiUtils.SnackbarDisplayType.NORMAL);
    }

    @Subscribe
    public void onEvent(final FlagChatMessageCommand cmd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.chat_flag_confirmation)
                .setPositiveButton(R.string.flag_confirm, (dialog, id) -> socialRepository.flagMessage(cmd.groupId, cmd.chatMessage.id)
                        .subscribe(aVoid -> {
                            MainActivity activity = (MainActivity) getActivity();
                            UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), "Flagged message by " + cmd.chatMessage.user, UiUtils.SnackbarDisplayType.NORMAL);
                        }, throwable -> {
                        }))
                .setNegativeButton(R.string.action_cancel, (dialog, id) -> {
                });
        builder.show();
    }

    @Subscribe
    public void onEvent(final ToggleLikeMessageCommand cmd) {
        socialRepository.likeMessage(cmd.groupId, cmd.chatMessage.id)
                .subscribe(voids -> {
                }, throwable -> {
                });
    }

    @Subscribe
    public void onEvent(final DeleteChatMessageCommand cmd) {
        socialRepository.deleteMessage(cmd.groupId, cmd.chatMessage.id)
                .subscribe(aVoid -> {}, throwable -> {});
    }

    @Subscribe
    public void onEvent(SendNewGroupMessageCommand cmd) {
        socialRepository.postGroupChat(cmd.targetGroupId, cmd.message)
                .subscribe(postChatMessageResult -> {}, throwable -> {});

        UiUtils.dismissKeyboard(getActivity());
    }

    // If the ChatList is Tavern, we're able to toggle the sleep-mode
    @Subscribe
    public void onEvent(ToggleInnCommand event) {
        userRepository.sleep(user)
                .subscribe(habitRPGUser -> {
                    ToggledInnStateEvent innState = new ToggledInnStateEvent();
                    innState.Inn = habitRPGUser.getPreferences().getSleep();
                    EventBus.getDefault().post(innState);
                }, throwable -> {
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("userId", this.userId);
        outState.putString("groupId", this.groupId);
        outState.putBoolean("isTavern", this.isTavern);
        super.onSaveInstanceState(outState);
    }

    public void setChatMessages(RealmResults<ChatMessage> chatMessages) {
        if (chatAdapter != null) {
            chatAdapter.updateData(chatMessages);
            recyclerView.scrollToPosition(0);
        }

        gotNewMessages = true;

        markMessagesAsSeen();
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
            socialRepository.postGroupChat(groupId, chatText).subscribe(postChatMessageResult -> {
                recyclerView.scrollToPosition(0);
            }, RxErrorHandler.handleEmptyError());
        }
    }

    @OnClick(R.id.emoji_button)
    public void openEmojiView() {

    }
}
