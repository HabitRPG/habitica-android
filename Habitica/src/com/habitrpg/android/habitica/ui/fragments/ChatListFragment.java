package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.DeleteChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.FlagChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.SendNewGroupMessageCommand;
import com.habitrpg.android.habitica.events.commands.ToggleInnCommand;
import com.habitrpg.android.habitica.events.commands.ToggleLikeMessageCommand;
import com.habitrpg.android.habitica.ui.adapter.ChatRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Negue on 14.09.2015.
 */
public class ChatListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Callback<List<ChatMessage>> {

    private Context ctx;
    private String groupId;
    private APIHelper apiHelper;
    private HabitRPGUser user;
    private String userId;
    private boolean isTavern;

    public ChatListFragment(Context ctx, String groupId, APIHelper apiHelper, HabitRPGUser user, boolean isTavern){

        this.ctx = ctx;
        this.groupId = groupId;
        this.apiHelper = apiHelper;
        this.user = user;
        this.userId = user.getId();
        this.isTavern = isTavern;

        // Receive Events
        EventBus.getDefault().register(this);
    }

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_chatlist, container, false);

        return view;
    }

    @InjectView(R.id.chat_list)
    RecyclerView mRecyclerView;

    @InjectView(R.id.chat_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    LinearLayoutManager layoutManager;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);


        layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(ctx);

            mRecyclerView.setLayoutManager(layoutManager);
        }

        ChatRecyclerViewAdapter tavernAdapter = new ChatRecyclerViewAdapter(new ArrayList<ChatMessage>(), ctx, userId, groupId, isTavern);

        mRecyclerView.setAdapter(tavernAdapter);

        onRefresh();
    }

    public void setRefreshEnabled(boolean enable) {
        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setEnabled(enable);
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);

        apiHelper.apiService.listGroupChat(groupId, this);
    }

    private List<ChatMessage> currentChatMessages;

    @Override
    public void success(List<ChatMessage> chatMessages, Response response) {
        currentChatMessages = chatMessages;

        // filter flagged messages
        for (int i = chatMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = chatMessages.get(i);

            if(msg.flagCount >= 2){
                chatMessages.remove(msg);
            }
        }

        ChatRecyclerViewAdapter tavernAdapter = new ChatRecyclerViewAdapter(chatMessages, ctx, userId, groupId, isTavern);

        mRecyclerView.setAdapter(tavernAdapter);

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void failure(RetrofitError error) {

    }


    private void showSnackbar(String msg, boolean negative){
        Snackbar snackbar = Snackbar.make(mRecyclerView, msg, Snackbar.LENGTH_LONG);

        if (negative) {
            View snackbarView = snackbar.getView();

            //change Snackbar's background color;
            snackbarView.setBackgroundColor(Color.RED);
        }

        snackbar.show();
    }

    public void onEvent(final FlagChatMessageCommand cmd){
        apiHelper.apiService.flagMessage(cmd.groupId, cmd.chatMessage.id, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                showSnackbar("Flagged message by " + cmd.chatMessage.user, false);
            }

            @Override
            public void failure(RetrofitError error) {
                showSnackbar("Failed to flag message by " + cmd.chatMessage.user, true);
            }
        });
    }

    public void onEvent(final ToggleLikeMessageCommand cmd){
        apiHelper.apiService.likeMessage(cmd.groupId, cmd.chatMessage.id, new Callback<List<Void>>() {
            @Override
            public void success(List<Void> aVoid, Response response) {
            }

            @Override
            public void failure(RetrofitError error) {
                showSnackbar("Failed to like message by " + cmd.chatMessage.user, true);
            }
        });
    }

    public void onEvent(final DeleteChatMessageCommand cmd) {
        apiHelper.apiService.deleteMessage(cmd.groupId, cmd.chatMessage.id, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                if (currentChatMessages != null) {
                    currentChatMessages.remove(cmd.chatMessage);

                    ChatListFragment.this.success(currentChatMessages, null);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void onEvent(SendNewGroupMessageCommand cmd) {
        apiHelper.apiService.postGroupChat(cmd.TargetGroupId, cmd.Message, new Callback<PostChatMessageResult>() {
            @Override
            public void success(PostChatMessageResult msg, Response response) {
                if (currentChatMessages != null) {
                    currentChatMessages.add(0, msg.message);

                    ChatListFragment.this.success(currentChatMessages, null);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    // If the ChatList is Tavern, we're able to toggle the sleep-mode
    public void onEvent(ToggleInnCommand event) {
        apiHelper.toggleSleep(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                ToggledInnStateEvent innState = new ToggledInnStateEvent();
                innState.Inn = !user.getPreferences().getSleep();

                user.getPreferences().setSleep(innState.Inn);

                EventBus.getDefault().post(innState);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}
