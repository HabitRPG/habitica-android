package com.habitrpg.android.habitica.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.user.User;

import javax.inject.Inject;

public class LocalNotificationActionReceiver extends BroadcastReceiver {
    @Inject
    public UserRepository userRepository;
    @Inject
    ApiClient apiClient;

    private User user;
    private String action;
    private Resources resources;
    private Intent intent;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        HabiticaBaseApplication.getComponent().inject(this);
        this.resources = context.getResources();

        this.action = intent.getAction();
        this.intent = intent;
        this.context = context;

        this.userRepository.getUser().first().subscribe(this::onUserReceived, RxErrorHandler.handleEmptyError());
    }

    public void onUserReceived(User user) {
        this.user = user;
        this.handleLocalNotificationAction(action);
        userRepository.close();
    }

    private void handleLocalNotificationAction(String action) {
        NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        //@TODO: This is a good place for a factory and event emitter pattern
        if (action.equals(this.resources.getString(R.string.accept_party_invite))) {
            if (this.user.getInvitations().getParty() == null) return;
            String partyId = this.user.getInvitations().getParty().getId();
            apiClient.joinGroup(partyId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
        } else if (action.equals(this.resources.getString(R.string.reject_party_invite))) {
            if (this.user.getInvitations().getParty() == null) return;
            String partyId = this.user.getInvitations().getParty().getId();
            apiClient.rejectGroupInvite(partyId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
        } else if (action.equals(this.resources.getString(R.string.accept_quest_invite))) {
            if (this.user.getParty() == null) return;
            String partyId = this.user.getParty().getId();
            apiClient.acceptQuest(partyId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
        } else if (action.equals(this.resources.getString(R.string.reject_quest_invite))) {
            if (this.user.getParty() == null) return;
            String partyId = this.user.getParty().getId();
            apiClient.rejectQuest(partyId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
        } else if (action.equals(this.resources.getString(R.string.accept_guild_invite))) {
            Bundle extras = this.intent.getExtras();
            String guildId = extras.getString("groupID");
            if (guildId == null) return;
            apiClient.joinGroup(guildId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
        } else if (action.equals(this.resources.getString(R.string.reject_guild_invite))) {
            Bundle extras = this.intent.getExtras();
            String guildId = extras.getString("groupID");
            if (guildId == null) return;
            apiClient.rejectGroupInvite(guildId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
        }
    }
}
