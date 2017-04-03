package com.habitrpg.android.habitica.ui.adapter.social;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.CopyChatAsTodoCommand;
import com.habitrpg.android.habitica.events.commands.CopyChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.DeleteChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.FlagChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.events.commands.OpenNewPMActivityCommand;
import com.habitrpg.android.habitica.events.commands.SendNewGroupMessageCommand;
import com.habitrpg.android.habitica.events.commands.SendNewInboxMessageCommand;
import com.habitrpg.android.habitica.events.commands.ToggleInnCommand;
import com.habitrpg.android.habitica.events.commands.ToggleLikeMessageCommand;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.EmojiKeyboard;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import net.pherth.android.emoji_library.EmojiEditText;
import net.pherth.android.emoji_library.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ChatRecyclerViewHolder> {
    private static final int TYPE_DANIEL = 0;
    private static final int TYPE_NEW_MESSAGE = 1;
    private static final int TYPE_MESSAGE = 2;

    private List<ChatMessage> messages;
    private HabitRPGUser user;
    private String uuid;
    private String groupId;
    private boolean isTavern;
    private boolean isInboxChat = false;
    private String replyToUserUUID;
    private HabitRPGUser sendingUser;

    public ChatRecyclerViewAdapter(List<ChatMessage> messages, HabitRPGUser user, String groupId, boolean isTavern) {
        this.messages = messages;
        this.user = user;
        if (user != null) this.uuid = user.getId();
        this.groupId = groupId;
        this.isTavern = isTavern;
    }

    public void setToInboxChat(String replyToUserUUID) {
        this.replyToUserUUID = replyToUserUUID;
        this.isInboxChat = true;
    }

    public void setSendingUser(HabitRPGUser user) {
        this.sendingUser = user;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0: {
                return isTavern ? TYPE_DANIEL : TYPE_NEW_MESSAGE;
            }
            case 1: {
                return isTavern ? TYPE_NEW_MESSAGE : TYPE_MESSAGE;
            }
            default: {
                return TYPE_MESSAGE;
            }
        }
    }

    @Override
    public ChatRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int rLayout = R.layout.tavern_chat_item;

        switch (viewType) {
            case TYPE_DANIEL: {
                rLayout = R.layout.tavern_daniel_item;
                break;
            }

            case TYPE_NEW_MESSAGE: {
                rLayout = R.layout.tavern_chat_new_entry_item;
                break;
            }
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(rLayout, parent, false);

        return new ChatRecyclerViewHolder(view, viewType, uuid, groupId);
    }

    @Override
    public void onBindViewHolder(ChatRecyclerViewHolder holder, int position) {
        if (!isTavern && position > 0) {
            holder.bind(messages.get(position - 1));
            return;
        }

        if (position > 1) {
            holder.bind(messages.get(position - 2));
        }
    }

    @Override
    public int getItemCount() {
        int messageCount = messages.size();

        if (isTavern) {
            // if there are no entries, we just show the toggle inn button
            messageCount += (messageCount == 0) ? 1 : 2;
        } else {
            messageCount += 1;
        }

        return messageCount;
    }

    public class ChatRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        // Toggle Inn State
        @BindView(R.id.btn_toggle_inn)
        @Nullable
        Button btnToggleInn;
        // New Msg
        @BindView(R.id.edit_new_message_text)
        @Nullable
        EmojiEditText textNewMessage;
        @BindView(R.id.btn_send_message)
        @Nullable
        Button btnSendNewMessage;
        @BindView(R.id.btn_options)
        @Nullable
        ImageView btnOptions;
        @BindView(R.id.user_background_layout)
        @Nullable
        LinearLayout userBackground;
        @BindView(R.id.like_background_layout)
        @Nullable
        LinearLayout likeBackground;
        @BindView(R.id.user_label)
        @Nullable
        TextView userLabel;
        @BindView(R.id.message_text)
        @Nullable
        EmojiTextView messageText;
        @BindView(R.id.ago_label)
        @Nullable
        TextView agoLabel;
        @BindView(R.id.tvLikes)
        @Nullable
        TextView tvLikes;
        Context context;
        Resources res;
        int likeCount = 0;
        boolean currentUserLikedPost = false;
        private int layoutType;
        private String uuid;
        private String groupId;
        private ChatMessage currentMsg;

        public ChatRecyclerViewHolder(View itemView, int layoutType, String currentUserId, String groupId) {
            super(itemView);
            this.layoutType = layoutType;
            this.uuid = currentUserId;
            this.groupId = groupId;

            ButterKnife.bind(this, itemView);

            context = itemView.getContext();

            res = context.getResources();

            switch (layoutType) {
                case TYPE_DANIEL: {
                    if (btnToggleInn != null) {
                        btnToggleInn.setOnClickListener(this);
                    }

                    ViewHelper.SetBackgroundTint(btnToggleInn, ContextCompat.getColor(context, R.color.brand));
                    if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences().getSleep()) {
                        btnToggleInn.setText(R.string.tavern_inn_checkOut);
                    } else {
                        btnToggleInn.setText(R.string.tavern_inn_rest);
                    }

                    break;
                }

                case TYPE_NEW_MESSAGE: {
                    if (btnSendNewMessage != null) {
                        btnSendNewMessage.setOnClickListener(this);
                    }
                    int color = ContextCompat.getColor(context, R.color.brand);

                    ViewHelper.SetBackgroundTint(btnSendNewMessage, color);

                    // Set up the emoji keyboard
                    EmojiKeyboard.createKeyboard(itemView, context);

                    break;
                }

                default: {
                    if (btnOptions != null) {
                        btnOptions.setOnClickListener(this);
                    }
                    if (tvLikes != null) {
                        tvLikes.setOnClickListener(this);
                    }
                }
            }
        }

        public void bind(final ChatMessage msg) {
            currentMsg = msg;

            if (layoutType != TYPE_DANIEL && layoutType != TYPE_NEW_MESSAGE) {
                setLikeProperties(msg);

                if (userBackground != null) {
                    if (msg.sent != null && msg.sent.equals("true")) {
                        DataBindingUtils.setRoundedBackgroundInt(userBackground, sendingUser.getContributor().getContributorColor());
                    } else {
                        DataBindingUtils.setRoundedBackgroundInt(userBackground, msg.getContributorColor());
                    }
                }

                if (msg.user == null || msg.user.equals("")) {
                    msg.user = "system";
                }

                if (userLabel != null) {
                    if (msg.sent != null && msg.sent.equals("true")) {
                        userLabel.setText(sendingUser.getProfile().getName());
                    } else {
                        userLabel.setText(msg.user);
                    }

                    userLabel.setClickable(true);
                    userLabel.setOnClickListener(view -> {
                        OpenFullProfileCommand cmd = new OpenFullProfileCommand(msg.uuid);

                        EventBus.getDefault().post(cmd);
                    });
                }

                DataBindingUtils.setForegroundTintColor(userLabel, msg.getContributorForegroundColor());

                if (messageText != null) {
                    messageText.setText(msg.parsedText);
                    if (msg.parsedText == null) {
                        messageText.setText(msg.text);
                    }
                    this.messageText.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (agoLabel != null) {
                    agoLabel.setText(msg.getAgoString(res));
                }
            }
        }

        private void setLikeProperties(ChatMessage msg) {
            likeCount = 0;
            currentUserLikedPost = false;

            if (msg != null && msg.likes != null) {
                for (Map.Entry<String, Boolean> e : msg.likes.entrySet()) {
                    if (e.getValue()) {
                        likeCount++;
                    }

                    if (e.getKey().equals(uuid)) {
                        currentUserLikedPost = true;
                    }
                }
            }

            setLikeProperties(likeCount);
        }

        private void setLikeProperties(int likeCount) {
            if (tvLikes != null) {
                tvLikes.setText("+" + likeCount);
            }

            int backgroundColorRes = 0;
            int foregroundColorRes = 0;

            if (likeCount != 0) {
                if (currentUserLikedPost) {
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

            DataBindingUtils.setRoundedBackground(likeBackground, ContextCompat.getColor(context, backgroundColorRes));
            tvLikes.setTextColor(ContextCompat.getColor(context, foregroundColorRes));
        }

        @Override
        public void onClick(View v) {
            if (currentMsg != null) {
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
                    } catch (Exception e) {
                    }

                    ChatMessage chatMsg = currentMsg;

                    if (!chatMsg.uuid.equals(uuid)) {
                        popupMenu.getMenu().findItem(R.id.menu_chat_delete).setVisible(false);
                    }
                    if (user.getContributor().getAdmin()) {
                        popupMenu.getMenu().findItem(R.id.menu_chat_delete).setVisible(true);
                    }
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
                    } catch (Exception e) {

                    }

                    return;
                }
            }

            if (tvLikes == v) {
                toggleLike();

                return;
            }


            if (v == btnToggleInn) {
                EventBus.getDefault().post(new ToggleInnCommand());
                if (!HabiticaApplication.User.getPreferences().getSleep()) {
                    if (btnToggleInn != null) {
                        btnToggleInn.setText(R.string.tavern_inn_checkOut);
                    }
                } else {
                    if (btnToggleInn != null) {
                        btnToggleInn.setText(R.string.tavern_inn_rest);
                    }
                }
                return;
            }

            if (textNewMessage != null) {
                String text = textNewMessage.getText().toString();
                if (!text.equals("")) {
                    if (isInboxChat) {
                        EventBus.getDefault().post(new SendNewInboxMessageCommand(replyToUserUUID, text));
                    } else {
                        EventBus.getDefault().post(new SendNewGroupMessageCommand(groupId, text));
                    }
                }
                textNewMessage.setText("");
            }
        }

        private void toggleLike() {
            int newCount = currentUserLikedPost ? --likeCount : ++likeCount;
            currentUserLikedPost = !currentUserLikedPost;

            setLikeProperties(newCount);

            EventBus.getDefault().post(new ToggleLikeMessageCommand(groupId, currentMsg));
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_chat_delete: {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.confirm_delete_tag_title)
                            .setMessage(R.string.confirm_delete_tag_message)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                                Toast.makeText(context, R.string.edit_tag_btn_done, Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new DeleteChatMessageCommand(groupId, currentMsg));
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                    break;
                }
                case R.id.menu_chat_flag: {
                    EventBus.getDefault().post(new FlagChatMessageCommand(groupId, currentMsg));

                    break;
                }
                case R.id.menu_chat_copy_as_todo: {
                    EventBus.getDefault().post(new CopyChatAsTodoCommand(groupId, currentMsg));

                    break;
                }

                case R.id.menu_chat_send_pm: {
                    EventBus.getDefault().post(new OpenNewPMActivityCommand());

                    break;
                }

                case R.id.menu_chat_copy: {
                    EventBus.getDefault().post(new CopyChatMessageCommand(groupId, currentMsg));

                    break;
                }
            }

            return false;
        }
    }
}
