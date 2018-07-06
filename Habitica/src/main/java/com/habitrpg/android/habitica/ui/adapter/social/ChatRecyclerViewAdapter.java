package com.habitrpg.android.habitica.ui.adapter.social;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import net.pherth.android.emoji_library.EmojiTextView;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class ChatRecyclerViewAdapter extends RealmRecyclerViewAdapter<ChatMessage, ChatRecyclerViewAdapter.ChatRecyclerViewHolder> {

    private User user;
    private boolean isTavern;
    private String uuid;
    private User sendingUser;

    private PublishSubject<ChatMessage> likeMessageEvents = PublishSubject.create();
    private PublishSubject<String> userLabelClickEvents = PublishSubject.create();
    private PublishSubject<String> privateMessageClickEvents = PublishSubject.create();
    private PublishSubject<ChatMessage> deleteMessageEvents = PublishSubject.create();
    private PublishSubject<ChatMessage> flatMessageEvents = PublishSubject.create();
    private PublishSubject<ChatMessage> copyMessageAsTodoEvents = PublishSubject.create();
    private PublishSubject<ChatMessage> copyMessageEvents = PublishSubject.create();

    public ChatRecyclerViewAdapter(@Nullable OrderedRealmCollection<ChatMessage> data, boolean autoUpdate, User user, boolean isTavern) {
        super(data, autoUpdate);
        this.user = user;
        this.isTavern = isTavern;
        if (user != null) this.uuid = user.getId();
    }

    public void setSendingUser(@Nullable User user) {
        this.sendingUser = user;
    }

    @Override
    public ChatRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tavern_chat_item, parent, false);
        return new ChatRecyclerViewHolder(view, uuid, isTavern);
    }

    @Override
    public void onBindViewHolder(ChatRecyclerViewHolder holder, int position) {
        if (getData() != null) {
            holder.bind(getData().get(position));
        }
    }

    public Flowable<ChatMessage> getLikeMessageEvents() {
        return likeMessageEvents.toFlowable(BackpressureStrategy.DROP);
    }

    public Flowable<String> getUserLabelClickEvents() {
        return userLabelClickEvents.toFlowable(BackpressureStrategy.DROP);
    }

    public Flowable<String> getPrivateMessageClickEvents() {
        return privateMessageClickEvents.toFlowable(BackpressureStrategy.DROP);
    }

    public Flowable<ChatMessage> getDeleteMessageEvents() {
        return deleteMessageEvents.toFlowable(BackpressureStrategy.DROP);
    }

    public Flowable<ChatMessage> getFlagMessageEvents() {
        return flatMessageEvents.toFlowable(BackpressureStrategy.DROP);
    }

    public Flowable<ChatMessage> getCopyMessageAsTodoEvents() {
        return copyMessageAsTodoEvents.toFlowable(BackpressureStrategy.DROP);
    }

    public Flowable<ChatMessage> getCopyMessageEvents() {
        return copyMessageEvents.toFlowable(BackpressureStrategy.DROP);
    }


    class ChatRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        @BindView(R.id.btn_options)
        ImageView btnOptions;
        @BindView(R.id.user_background_layout)
        LinearLayout userBackground;
        @BindView(R.id.user_label)
        TextView userLabel;
        @BindView(R.id.message_text)
        EmojiTextView messageText;
        @BindView(R.id.ago_label)
        TextView agoLabel;
        @BindView(R.id.like_background_layout)
        LinearLayout likeBackground;
        @BindView(R.id.tvLikes)
        TextView tvLikes;

        Context context;
        Resources res;
        private String userId;
        private boolean isTavern;
        private ChatMessage chatMessage;

        ChatRecyclerViewHolder(View itemView, String currentUserId, boolean isTavern) {
            super(itemView);
            this.userId = currentUserId;
            this.isTavern = isTavern;

            ButterKnife.bind(this, itemView);

            context = itemView.getContext();

            res = context.getResources();

            if (btnOptions != null) {
                btnOptions.setOnClickListener(this);
            }
        }

        public void bind(final ChatMessage msg) {
            chatMessage = msg;

            setLikeProperties();

            if (userBackground != null) {
                if (msg.getSent() != null && msg.getSent().equals("true") && sendingUser != null) {
                    DataBindingUtils.INSTANCE.setRoundedBackgroundInt(userBackground, sendingUser.getContributorColor());
                } else {
                    DataBindingUtils.INSTANCE.setRoundedBackgroundInt(userBackground, msg.getContributorColor());
                }
            }

            if (userLabel != null) {
                if (msg.getSent() != null && msg.getSent().equals("true")) {
                    userLabel.setText(sendingUser.getProfile().getName());
                } else {
                    if (msg.getUser() != null && msg.getUser().length() > 0) {
                        userLabel.setText(msg.getUser());
                    } else {
                        userLabel.setText(R.string.system);
                    }
                }

                userLabel.setClickable(true);
                userLabel.setOnClickListener(view -> userLabelClickEvents.onNext(msg.getUuid()));
            }

            DataBindingUtils.INSTANCE.setForegroundTintColor(userLabel, msg.getContributorForegroundColor());

            if (messageText != null) {
                messageText.setText(chatMessage.getParsedText());
                if (msg.getParsedText() == null) {
                    messageText.setText(chatMessage.getText());
                    Maybe.just(chatMessage.getText())
                            .map(MarkdownParser.INSTANCE::parseMarkdown)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(parsedText -> {
                                chatMessage.setParsedText(parsedText);
                                messageText.setText(chatMessage.getParsedText());
                            }, Throwable::printStackTrace);
                }
                this.messageText.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if (agoLabel != null) {
                agoLabel.setText(msg.getAgoString(res));
            }
        }

        private void setLikeProperties() {
            likeBackground.setVisibility(isTavern ? View.VISIBLE : View.INVISIBLE);
            tvLikes.setText("+" + chatMessage.getLikeCount());

            int backgroundColorRes;
            int foregroundColorRes;

            if (chatMessage.getLikeCount() != 0) {
                if (chatMessage.userLikesMessage(userId)) {
                    backgroundColorRes = R.color.tavern_userliked_background;
                    foregroundColorRes = R.color.tavern_userliked_foreground;
                } else {
                    backgroundColorRes = R.color.tavern_somelikes_background;
                    foregroundColorRes = R.color.tavern_somelikes_foreground;
                }
            } else {
                backgroundColorRes = R.color.tavern_nolikes_background;
                foregroundColorRes = R.color.tavern_nolikes_foreground;
            }

            DataBindingUtils.INSTANCE.setRoundedBackground(likeBackground, ContextCompat.getColor(context, backgroundColorRes));
            tvLikes.setTextColor(ContextCompat.getColor(context, foregroundColorRes));
        }

        @Override
        public void onClick(View v) {
            if (chatMessage != null) {
                if (btnOptions == v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);

                    //set my own listener giving the View that activates the event onClick (i.e. YOUR ImageView)
                    popupMenu.setOnMenuItemClickListener(this);
                    //inflate your PopUpMenu
                    popupMenu.getMenuInflater().inflate(R.menu.chat_message, popupMenu.getMenu());

                    // Force icons to show
                    Object menuHelper = null;
                    Class[] argTypes;
                    try {
                        Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                        fMenuHelper.setAccessible(true);
                        menuHelper = fMenuHelper.get(popupMenu);
                        argTypes = new Class[]{boolean.class};
                        menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
                    } catch (Exception ignored) {
                    }


                    popupMenu.getMenu().findItem(R.id.menu_chat_delete).setVisible(shouldShowDelete(chatMessage));
                    popupMenu.getMenu().findItem(R.id.menu_chat_flag).setVisible(!chatMessage.getUuid().equals("system"));
                    popupMenu.getMenu().findItem(R.id.menu_chat_copy_as_todo).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.menu_chat_send_pm).setVisible(false);

                    popupMenu.show();

                    // Try to force some horizontal offset
                    try {
                        Field fListPopup = menuHelper.getClass().getDeclaredField("mPopup");
                        fListPopup.setAccessible(true);
                        Object listPopup = fListPopup.get(menuHelper);
                        argTypes = new Class[]{int.class};
                        Class listPopupClass = listPopup.getClass();

                        // Get the width of the popup window
                        int width = (Integer) listPopupClass.getDeclaredMethod("getWidth").invoke(listPopup);

                        // Invoke setHorizontalOffset() with the negative width to move left by that distance
                        listPopupClass.getDeclaredMethod("setHorizontalOffset", argTypes).invoke(listPopup, -width);

                        // Invoke show() to update the window's position
                        listPopupClass.getDeclaredMethod("show").invoke(listPopup);
                    } catch (Exception ignored) {

                    }
                }
            }
        }

        private boolean shouldShowDelete(ChatMessage chatMsg) {
            return !chatMsg.isSystemMessage() && (chatMsg.getUuid().equals(userId) || user.getContributor() != null && user.getContributor().getAdmin());
        }

        @OnClick(R.id.tvLikes)
        public void toggleLike() {
            likeMessageEvents.onNext(chatMessage);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_chat_delete: {
                    deleteMessageEvents.onNext(chatMessage);
                    break;
                }
                case R.id.menu_chat_flag: {
                    flatMessageEvents.onNext(chatMessage);
                    break;
                }
                case R.id.menu_chat_copy_as_todo: {
                    copyMessageAsTodoEvents.onNext(chatMessage);
                    break;
                }

                case R.id.menu_chat_send_pm: {
                    privateMessageClickEvents.onNext(chatMessage.getUuid());
                    break;
                }

                case R.id.menu_chat_copy: {
                    copyMessageEvents.onNext(chatMessage);
                    break;
                }
            }

            return false;
        }
    }
}
