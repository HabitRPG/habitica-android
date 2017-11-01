package com.habitrpg.android.habitica.ui.fragments.social;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.realm.RealmResults;

import static com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.showSnackbar;
import static com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType;

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
    @BindView(R.id.community_guidelines_view)
    TextView communityGuidelinesView;
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

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_chat, container, false);
        }

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
            //layoutManager.setReverseLayout(true);
            //layoutManager.setStackFromEnd(false);

            recyclerView.setLayoutManager(layoutManager);
        }

        chatAdapter = new ChatRecyclerViewAdapter(null, true, user, true);
        compositeSubscription.add(chatAdapter.getUserLabelClickEvents().subscribe(userId -> FullProfileActivity.open(getContext(), userId), RxErrorHandler.handleEmptyError()));
        compositeSubscription.add(chatAdapter.getDeleteMessageEvents().subscribe(this::showDeleteConfirmationDialog, RxErrorHandler.handleEmptyError()));
        compositeSubscription.add(chatAdapter.getFlatMessageEvents().subscribe(this::showFlagConfirmationDialog, RxErrorHandler.handleEmptyError()));
        compositeSubscription.add(chatAdapter.getCopyMessageAsTodoEvents().subscribe(this::copyMessageAsTodo, RxErrorHandler.handleEmptyError()));
        compositeSubscription.add(chatAdapter.getCopyMessageEvents().subscribe(this::copyMessageToClipboard, RxErrorHandler.handleEmptyError()));
        compositeSubscription.add(chatAdapter.getLikeMessageEvents().flatMap(socialRepository::likeMessage).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError()));

        recyclerView.setAdapter(chatAdapter);
        recyclerView.setItemAnimator(new SafeDefaultItemAnimator());

        socialRepository.getGroupChat(groupId).first().subscribe(this::setChatMessages, RxErrorHandler.handleEmptyError());

        if (user != null && user.getFlags() != null && user.getFlags().isCommunityGuidelinesAccepted()) {
            communityGuidelinesView.setVisibility(View.GONE);
        } else {
            communityGuidelinesView.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://habitica.com/static/community-guidelines"));
                getContext().startActivity(i);

                userRepository.updateUser(user, "flags.communityGuidelinesAccepted", true).subscribe(user1 -> {}, RxErrorHandler.handleEmptyError());
            });
        }
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

    public void copyMessageToClipboard(ChatMessage chatMessage) {
        ClipboardManager clipMan = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData messageText = ClipData.newPlainText("Chat message", chatMessage.text);
        clipMan.setPrimaryClip(messageText);
        MainActivity activity = (MainActivity) getActivity();
        showSnackbar(activity.getFloatingMenuWrapper(), getString(R.string.chat_message_copied), SnackbarDisplayType.NORMAL);
    }

    public void showFlagConfirmationDialog(ChatMessage chatMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.chat_flag_confirmation)
                .setPositiveButton(R.string.flag_confirm, (dialog, id) -> socialRepository.flagMessage(chatMessage)
                        .subscribe(aVoid -> {
                            MainActivity activity = (MainActivity) getActivity();
                            showSnackbar(activity.getFloatingMenuWrapper(), "Flagged message by " + chatMessage.user, SnackbarDisplayType.NORMAL);
                        }, RxErrorHandler.handleEmptyError()))
                .setNegativeButton(R.string.action_cancel, (dialog, id) -> {});
        builder.show();
    }

    private void showDeleteConfirmationDialog(ChatMessage chatMessage) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.confirm_delete_tag_title)
                .setMessage(R.string.confirm_delete_tag_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> socialRepository.deleteMessage(chatMessage).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError()))
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void copyMessageAsTodo(ChatMessage chatMessage) {

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
