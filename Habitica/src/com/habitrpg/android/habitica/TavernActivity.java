package com.habitrpg.android.habitica;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.habitrpg.android.habitica.events.commands.CopyChatAsTodoCommand;
import com.habitrpg.android.habitica.events.commands.DeleteChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.FlagChatMessageCommand;
import com.habitrpg.android.habitica.events.commands.OpenNewPMActivityCommand;
import com.habitrpg.android.habitica.events.commands.SendNewGroupMessageCommand;
import com.habitrpg.android.habitica.events.commands.ToggleInnCommand;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.ToggleLikeMessageCommand;
import com.habitrpg.android.habitica.prefs.PrefsActivity;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.adapter.TavernRecyclerViewAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;
import com.mikepenz.materialdrawer.Drawer;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TavernActivity extends AppCompatActivity implements Callback<List<ChatMessage>> {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.avatar)
    LinearLayout avatarHeader;

    @InjectView(R.id.tavern_list)
    RecyclerView recyclerView;

    private AvatarWithBarsViewModel avatarInHeader;
    private APIHelper mAPIHelper;
    private HabitRPGUser User;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tavern);

        ButterKnife.inject(this);

        setSupportActionBar(toolbar);


        // Receive Events
        EventBus.getDefault().register(this);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(false);

            toolbar.setTitleTextColor(this.getResources().getColor(R.color.white));
        }

        Drawer drawer = MainDrawerBuilder.CreateDefaultBuilderSettings(this, toolbar)
                .withTranslucentNavigationBar(false)
                .withTranslucentStatusBar(false)
                .withDisplayBelowStatusBar(false)
                .withDisplayBelowToolbar(false)
                .withSelectedItem(2)
                .build();

        avatarInHeader = new AvatarWithBarsViewModel(this, avatarHeader);

        HostConfig hostConfig = PrefsActivity.fromContext(this);
        User = new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).querySingle();

        avatarInHeader.UpdateData(User);

        mAPIHelper = new APIHelper(this, hostConfig);

        mAPIHelper.apiService.listGroupChat("habitrpg", this);
    }

    private void showSnackbar(String msg, boolean negative){
        Snackbar snackbar = Snackbar.make(recyclerView, msg, Snackbar.LENGTH_LONG);

        if (negative) {
            View snackbarView = snackbar.getView();

            //change Snackbar's background color;
            snackbarView.setBackgroundColor(getResources().getColor(R.color.red));
        }

        snackbar.show();
    }

    public void onEvent(final FlagChatMessageCommand cmd){
        mAPIHelper.apiService.flagMessage(cmd.groupId, cmd.chatMessage.id, new Callback<Void>() {
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
        mAPIHelper.apiService.likeMessage(cmd.groupId, cmd.chatMessage.id, new Callback<List<Void>>() {
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
        mAPIHelper.apiService.deleteMessage(cmd.groupId, cmd.chatMessage.id, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                if (currentChatMessages != null) {
                    currentChatMessages.remove(cmd.chatMessage);

                    TavernActivity.this.success(currentChatMessages, null);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void onEvent(SendNewGroupMessageCommand cmd) {
        mAPIHelper.apiService.postGroupChat(cmd.TargetGroupId, cmd.Message, new Callback<PostChatMessageResult>() {
            @Override
            public void success(PostChatMessageResult msg, Response response) {
                if (currentChatMessages != null) {
                    currentChatMessages.add(0, msg.message);

                    TavernActivity.this.success(currentChatMessages, null);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void onEvent(ToggleInnCommand event) {
        mAPIHelper.toggleSleep(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                ToggledInnStateEvent innState = new ToggledInnStateEvent();
                innState.Inn = !User.getPreferences().getSleep();

                User.getPreferences().setSleep(innState.Inn);

                avatarInHeader.UpdateData(User);
                EventBus.getDefault().post(innState);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        TavernRecyclerViewAdapter tavernAdapter = new TavernRecyclerViewAdapter(chatMessages, this, User.getId());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(tavernAdapter);
    }

    @Override
    public void failure(RetrofitError error) {

        showSnackbar(error.getMessage(), true);
    }

}
