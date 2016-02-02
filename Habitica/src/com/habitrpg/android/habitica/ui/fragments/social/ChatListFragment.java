package com.habitrpg.android.habitica.ui.fragments.social;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.PrefsActivity;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.DeleteChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.FlagChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.SendNewGroupMessageCommand;
import com.habitrpg.android.habitica.events.commands.ToggleInnCommand;
import com.habitrpg.android.habitica.events.commands.ToggleLikeMessageCommand;
import com.habitrpg.android.habitica.ui.UiUtils;
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;
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
    private MainActivity activity;
    private boolean registerEventBus = false;

    public void configure(Context ctx, String groupId, APIHelper apiHelper, HabitRPGUser user, MainActivity activity, boolean isTavern) {

        this.ctx = ctx;
        this.groupId = groupId;
        this.apiHelper = apiHelper;
        this.user = user;
        if (this.user != null) {
            this.userId = this.user.getId();
        }
        this.isTavern = isTavern;
        this.activity = activity;
    }

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
              this.user = new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(userId)).querySingle();
            }
          }

        }

        if (view == null)
            view = inflater.inflate(R.layout.fragment_chatlist, container, false);

        // Receive Events
        try {
            EventBus.getDefault().register(this);
            registerEventBus = true;
        } catch (EventBusException ignored) {

        }

        if (apiHelper == null) {
            apiHelper = new APIHelper(PrefsActivity.fromContext(getContext()));
        }

        return view;
    }

    @Bind(R.id.chat_list)
    RecyclerView mRecyclerView;

    @Bind(R.id.chat_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    LinearLayoutManager layoutManager;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

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

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);

        apiHelper.apiService.listGroupChat(groupId, this);
    }

    private List<ChatMessage> currentChatMessages;

    @Override
    public void success(List<ChatMessage> chatMessages, Response response) {
        currentChatMessages = chatMessages;

        ChatRecyclerViewAdapter tavernAdapter = new ChatRecyclerViewAdapter(chatMessages, ctx, userId, groupId, isTavern);

        if(mRecyclerView != null) {
            mRecyclerView.setAdapter(tavernAdapter);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void failure(RetrofitError error) {

    }

    public void onEvent(final FlagChatMessageCommand cmd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.chat_flag_confirmation)
                .setPositiveButton(R.string.flag_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        apiHelper.apiService.flagMessage(cmd.groupId, cmd.chatMessage.id, new Callback<Void>() {
                            @Override
                            public void success(Void aVoid, Response response) {
                                UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), "Flagged message by " + cmd.chatMessage.user, UiUtils.SnackbarDisplayType.NORMAL);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                            }
                        });                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.show();
    }

    public void onEvent(final ToggleLikeMessageCommand cmd) {
        apiHelper.apiService.likeMessage(cmd.groupId, cmd.chatMessage.id, new Callback<List<Void>>() {
            @Override
            public void success(List<Void> aVoid, Response response) {
            }

            @Override
            public void failure(RetrofitError error) {

                //showSnackbar("Failed to like message by " + cmd.chatMessage.user, true);
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

        UiUtils.dismissKeyboard(HabiticaApplication.currentActivity);
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

    @Override
    public void onDestroyView() {
        if (registerEventBus) {
            EventBus.getDefault().unregister(this);
        }
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("userId", this.userId);
        outState.putString("groupId", this.groupId);
        outState.putBoolean("isTavern", this.isTavern);
        super.onSaveInstanceState(outState);
    }

}
