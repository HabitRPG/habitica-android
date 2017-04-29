package com.habitrpg.android.habitica.ui.adapter.social;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.CopyChatAsTodoCommand;
import com.habitrpg.android.habitica.events.commands.CopyChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.DeleteChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.FlagChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.OpenFullProfileCommand;
import com.habitrpg.android.habitica.events.commands.OpenNewPMActivityCommand;
import com.habitrpg.android.habitica.events.commands.ToggleLikeMessageCommand;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;

import net.pherth.android.emoji_library.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ChatRecyclerViewHolder> {

    private List<ChatMessage> messages;
    private User user;
    private String uuid;
    private String groupId;
    private boolean isInboxChat = false;
    private String replyToUserUUID;
    private User sendingUser;

    public ChatRecyclerViewAdapter(List<ChatMessage> messages, User user, String groupId) {
        this.messages = messages;
        this.user = user;
        if (user != null) this.uuid = user.getId();
        this.groupId = groupId;
    }

    public void setToInboxChat(String replyToUserUUID) {
        this.replyToUserUUID = replyToUserUUID;
        this.isInboxChat = true;
    }

    public void setSendingUser(@Nullable User user) {
        this.sendingUser = user;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }

    @Override
    public ChatRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tavern_chat_item, parent, false);
        return new ChatRecyclerViewHolder(view, viewType, uuid, groupId);
    }

    @Override
    public void onBindViewHolder(ChatRecyclerViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public class ChatRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        @BindView(R.id.btn_options)
        ImageView btnOptions;
        @BindView(R.id.user_background_layout)
        LinearLayout userBackground;
        @BindView(R.id.like_background_layout)
        LinearLayout likeBackground;
        @BindView(R.id.user_label)
        TextView userLabel;
        @BindView(R.id.message_text)
        EmojiTextView messageText;
        @BindView(R.id.ago_label)
        TextView agoLabel;
        @BindView(R.id.tvLikes)
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

            if (btnOptions != null) {
                btnOptions.setOnClickListener(this);
            }
            if (tvLikes != null) {
                tvLikes.setOnClickListener(this);
            }
        }

        public void bind(final ChatMessage msg) {
            currentMsg = msg;

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

            int backgroundColorRes;
            int foregroundColorRes;

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
