package com.habitrpg.android.habitica.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;

import javax.inject.Inject;

/**
 * Created by keithholliday on 6/30/16.
 */
public class LocalNotificationActionReceiver extends BroadcastReceiver implements HabitRPGUserCallback.OnUserReceived {
    @Inject
    public ApiClient apiClient;

    private HabitRPGUser user;
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

        this.apiClient.getUser()
                .subscribe(new HabitRPGUserCallback(this), throwable -> {
                });
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        this.user = user;
        this.handleLocalNotificationAction(action);
    }

    private void handleLocalNotificationAction(String action) {
        NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        //@TODO: This is a good place for a factory and event emitter pattern
        if (action.equals(this.resources.getString(R.string.accept_party_invite))) {
            if (this.user.getInvitations().getParty() == null) return;
            String partyId = this.user.getInvitations().getParty().getId();
            apiClient.joinGroup(partyId)

                    .subscribe(aVoid -> {
                    }, throwable -> {
                    });
        } else if (action.equals(this.resources.getString(R.string.reject_party_invite))) {
            if (this.user.getInvitations().getParty() == null) return;
            String partyId = this.user.getInvitations().getParty().getId();
            apiClient.rejectGroupInvite(partyId)

                    .subscribe(aVoid -> {
                    }, throwable -> {
                    });
        } else if (action.equals(this.resources.getString(R.string.accept_quest_invite))) {
            if (this.user.getParty() == null) return;
            String partyId = this.user.getParty().getId();
            apiClient.acceptQuest(partyId)

                    .subscribe(aVoid -> {
                    }, throwable -> {
                    });
        } else if (action.equals(this.resources.getString(R.string.reject_quest_invite))) {
            if (this.user.getParty() == null) return;
            String partyId = this.user.getParty().getId();
            apiClient.rejectQuest(partyId)

                    .subscribe(aVoid -> {
                    }, throwable -> {
                    });
        } else if (action.equals(this.resources.getString(R.string.accept_guild_invite))) {
            Bundle extras = this.intent.getExtras();
            String guildId = extras.getString("groupID");
            if (guildId == null) return;
            apiClient.joinGroup(guildId)

                    .subscribe(aVoid -> {
                    }, throwable -> {
                    });
        } else if (action.equals(this.resources.getString(R.string.reject_guild_invite))) {
            Bundle extras = this.intent.getExtras();
            String guildId = extras.getString("groupID");
            if (guildId == null) return;
            apiClient.rejectGroupInvite(guildId)

                    .subscribe(aVoid -> {
                    }, throwable -> {
                    });
        }
    }
}
