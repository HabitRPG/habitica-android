package com.habitrpg.android.habitica.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import javax.inject.Inject;

/**
 * Created by keithholliday on 6/30/16.
 */
public class LocalNotificationActionReceiver extends BroadcastReceiver implements HabitRPGUserCallback.OnUserReceived {
    @Inject
    public APIHelper apiHelper;

    private HabitRPGUser user;
    private String action;
    private Resources resources;
    private Intent intent;
    private Context conext;

    @Override
    public void onReceive(Context context, Intent intent) {
        HabiticaApplication.getInstance(context).getComponent().inject(this);
        this.resources = context.getResources();

        this.action = intent.getAction();
        this.intent = intent;
        this.conext = conext;

        this.apiHelper.apiService.getUser()
                .compose(this.apiHelper.configureApiCallObserver())
                .subscribe(new HabitRPGUserCallback(this), throwable -> {});
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        this.user = user;
        this.handleLocalNotificationAction(action);
    }

    private void handleLocalNotificationAction(String action) {
        //@TODO: This is a good place for a factory and event emitter pattern
        if (action.equals(this.resources.getString(R.string.accept_party_invite))) {
            if (this.user.getInvitations().getParty() == null) return;
            String partyId = this.user.getInvitations().getParty().getId();
            apiHelper.apiService.joinGroup(partyId)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(aVoid -> {}, throwable -> {});
        } else if (action.equals(this.resources.getString(R.string.reject_party_invite))) {
            if (this.user.getInvitations().getParty() == null) return;
            String partyId = this.user.getInvitations().getParty().getId();
            apiHelper.apiService.rejectGroupInvite(partyId)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(aVoid -> {}, throwable -> {});
        } else if (action.equals(this.resources.getString(R.string.accept_quest_invite))) {
            if (this.user.getParty() == null) return;
            String partyId = this.user.getParty().getId();
            apiHelper.apiService.acceptQuest(partyId)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(aVoid -> {}, throwable -> {});
        } else if (action.equals(this.resources.getString(R.string.reject_quest_invite))) {
            if (this.user.getParty() == null) return;
            String partyId = this.user.getParty().getId();
            apiHelper.apiService.rejectQuest(partyId)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(aVoid -> {}, throwable -> {});
        } else if (action.equals(this.resources.getString(R.string.accept_guild_invite))) {
            Bundle extras = this.intent.getExtras();
            String guildId = extras.getString("groupID");
            if (guildId == null) return;
            apiHelper.apiService.joinGroup(guildId)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(aVoid -> {}, throwable -> {});
        } else if (action.equals(this.resources.getString(R.string.reject_guild_invite))) {
            Bundle extras = this.intent.getExtras();
            String guildId = extras.getString("groupID");
            if (guildId == null) return;
            apiHelper.apiService.rejectGroupInvite(guildId)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(aVoid -> {}, throwable -> {});
        }

        NotificationManager notificationManager = (NotificationManager) this.conext.getSystemService(this.conext.NOTIFICATION_SERVICE);
        notificationManager.cancel(10);
    }
}
