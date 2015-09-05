package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.CopyChatAsTodoCommand;
import com.habitrpg.android.habitica.events.commands.DeleteChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.FlagChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.OpenNewPMActivityCommand;
import com.habitrpg.android.habitica.events.commands.SendNewGroupMessageCommand;
import com.habitrpg.android.habitica.events.commands.ToggleInnCommand;
import com.habitrpg.android.habitica.events.commands.ToggleLikeMessageCommand;
import com.habitrpg.android.habitica.ui.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.typeface.FontAwesome;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import de.greenrobot.event.EventBus;

/**
 * Created by Negue on 20.08.2015.
 */
public class TavernRecyclerViewAdapter extends RecyclerView.Adapter<TavernRecyclerViewAdapter.TavernRecyclerViewHolder> {
    static final int TYPE_DANIEL = 0;
    static final int TYPE_NEW_MESSAGE = 1;
    static final int TYPE_MESSAGE = 2;

    private List<ChatMessage> messages;
    private Context viewContext;
    private String uuid;

    public TavernRecyclerViewAdapter(List<ChatMessage> messages, Context viewContext, String uuid) {
        this.messages = messages;
        this.viewContext = viewContext;
        this.uuid = uuid;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0: {
                return TYPE_DANIEL;
            }
            case 1: {
                return TYPE_NEW_MESSAGE;
            }
            default: {
                return TYPE_MESSAGE;
            }
        }
    }

    @Override
    public TavernRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

        return new TavernRecyclerViewHolder(view, viewType, viewContext, uuid, "habitrpg");
    }

    @Override
    public void onBindViewHolder(TavernRecyclerViewHolder holder, int position) {
        if (position > 1) {
            holder.bind(messages.get(position - 2));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size() + 2;
    }

    public class TavernRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private int layoutType;
        private String uuid;
        private String groupId;

        // Toggle Inn State
        @InjectView(R.id.btn_toggle_inn)
        @Optional
        Button btnToggleInn;

        // New Msg
        @InjectView(R.id.edit_new_message_text)
        @Optional
        AppCompatEditText textNewMessage;

        @InjectView(R.id.btn_send_message)
        @Optional
        Button btnSendNewMessage;

        @InjectView(R.id.btn_options)
        @Optional
        ImageView btnOptions;

        @InjectView(R.id.user_background_layout)
        @Optional
        LinearLayout userBackground;

        @InjectView(R.id.like_background_layout)
        @Optional
        LinearLayout likeBackground;

        @InjectView(R.id.user_label)
        @Optional
        TextView userLabel;

        @InjectView(R.id.message_text)
        @Optional
        TextView messageText;

        @InjectView(R.id.ago_label)
        @Optional
        TextView agoLabel;

        @InjectView(R.id.tvLikes)
        @Optional
        TextView tvLikes;

        Context context;
        Resources res;

        public TavernRecyclerViewHolder(View itemView, int layoutType, Context viewContext, String currentUserId, String groupId) {
            super(itemView);
            this.layoutType = layoutType;
            this.uuid = currentUserId;
            this.groupId = groupId;

            ButterKnife.inject(this, itemView);

            context = viewContext;

            res = context.getResources();

            switch (layoutType) {
                case TYPE_DANIEL: {
                    btnToggleInn.setOnClickListener(this);

                    ViewHelper.SetBackgroundTint(btnToggleInn, res.getColor(R.color.brand));

                    break;
                }

                case TYPE_NEW_MESSAGE: {
                    btnSendNewMessage.setOnClickListener(this);
                    int color = res.getColor(R.color.brand);

                    // Using the Iconics buttons, it is unable to tint the background
                    btnSendNewMessage.setTypeface(Iconics.findFont(FontAwesome.Icon.faw_comment).getTypeface(context));
                    btnSendNewMessage.setText(new Iconics.IconicsBuilder().ctx(context).on("{faw-comment}").build());

                    ViewHelper.SetBackgroundTint(btnSendNewMessage, color);

                    break;
                }

                default: {
                    btnOptions.setOnClickListener(this);
                    tvLikes.setOnClickListener(this);
                }
            }
        }

        private ChatMessage currentMsg;

        public void bind(final ChatMessage msg) {
            currentMsg = msg;

            if (layoutType != TYPE_DANIEL && layoutType != TYPE_NEW_MESSAGE) {
                setLikeProperties(msg);

                DataBindingUtils.setRoundedBackgroundInt(userBackground, msg.getContributorColor());

                userLabel.setText(msg.user);
                DataBindingUtils.setForegroundTintColor(userLabel, msg.getContributorForegroundColor());

                messageText.setText(msg.text.trim());
                agoLabel.setText(msg.getAgoString());
            }
        }

        int likeCount = 0;
        boolean currentUserLikedPost = false;

        private void setLikeProperties(ChatMessage msg) {
            likeCount = 0;
            currentUserLikedPost = false;

            for (Map.Entry<String, Boolean> e : msg.likes.entrySet()) {
                if (e.getValue()) {
                    likeCount++;
                }

                if (e.getKey().equals(uuid)) {
                    currentUserLikedPost = true;
                }
            }

            setLikeProperties(likeCount);
        }

        private void setLikeProperties(int likeCount) {
            tvLikes.setText("+" + likeCount);

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

            DataBindingUtils.setRoundedBackground(likeBackground, res.getColor(backgroundColorRes));
            tvLikes.setTextColor(res.getColor(foregroundColorRes));
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
                return;
            }

            String text = textNewMessage.getText().toString();

            if(!text.equals("")) {
                EventBus.getDefault().post(new SendNewGroupMessageCommand(groupId, text));
            }

            textNewMessage.setText("");
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
                    EventBus.getDefault().post(new DeleteChatMessageCommand(groupId, currentMsg));

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
            }

            return false;
        }
    }
}
