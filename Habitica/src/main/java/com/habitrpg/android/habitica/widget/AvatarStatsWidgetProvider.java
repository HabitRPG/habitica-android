package com.habitrpg.android.habitica.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.activities.MainActivity;

import javax.inject.Inject;
import javax.inject.Named;

public class AvatarStatsWidgetProvider extends BaseWidgetProvider {

    private AppWidgetManager appWidgetManager;

    @Override
    public int layoutResourceId() {
        return R.layout.widget_avatar_stats;
    }

    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;
    @Inject
    UserRepository userRepository;

    private void setUp() {
        if (userRepository == null) {
            HabiticaBaseApplication.getComponent().inject(this);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.setUp();
        this.appWidgetManager = appWidgetManager;
        this.context = context;

        userRepository.getUser(userId).subscribe(this::updateData, throwable -> {});
    }

    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {
        if (columns > 3) {
            remoteViews.setViewVisibility(R.id.avatar_view, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.avatar_view, View.GONE);
        }

        if (rows > 1) {
            remoteViews.setViewVisibility(R.id.mp_wrapper, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.detail_info_view, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.mp_wrapper, View.GONE);
            remoteViews.setViewVisibility(R.id.detail_info_view, View.GONE);
        }

        return remoteViews;
    }

    private void updateData(User user) {
        if (user == null || user.getStats() == null) {
            return;
        }
        Stats stats = user.getStats();
        ComponentName thisWidget = new ComponentName(context, AvatarStatsWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        String healthValueString = "" + stats.getHp().intValue() + "/" + stats.getMaxHealth();
        String expValueString = "" + stats.getExp().intValue() + "/" + stats.getToNextLevel();
        String mpValueString = "" + stats.getMp().intValue() + "/" + stats.getMaxMP();

        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_avatar_stats);

            remoteViews.setTextViewText(R.id.TV_hp_value, healthValueString);
            remoteViews.setTextViewText(R.id.exp_TV_value, expValueString);
            remoteViews.setTextViewText(R.id.mp_TV_value, mpValueString);

            remoteViews.setProgressBar(R.id.hp_bar, stats.getMaxHealth(), stats.getHp().intValue(), false);
            remoteViews.setProgressBar(R.id.exp_bar, stats.getToNextLevel(), stats.getExp().intValue(), false);
            remoteViews.setProgressBar(R.id.mp_bar, stats.getMaxMP(), stats.getMp().intValue(), false);
            remoteViews.setViewVisibility(R.id.mp_wrapper, (stats.getHabitClass() == null || stats.getLvl() < 10 || user.getPreferences().getDisableClasses()) ? View.GONE : View.VISIBLE);

            int gp = (stats.getGp().intValue());
            int sp = (int) ((stats.getGp() - gp) * 100);
            remoteViews.setTextViewText(R.id.gold_tv, String.valueOf(gp));
            remoteViews.setTextViewText(R.id.silver_tv, String.valueOf(sp));
            remoteViews.setTextViewText(R.id.gems_tv, String.valueOf((int) (user.getBalance() * 4)));
            remoteViews.setTextViewText(R.id.lvl_tv, context.getString(R.string.user_level, user.getStats().getLvl()));

            AvatarView avatarView = new AvatarView(context, true, true, true);

            avatarView.setUser(user);
            RemoteViews finalRemoteViews = remoteViews;
            avatarView.onAvatarImageReady(bitmap -> {
                finalRemoteViews.setImageViewBitmap(R.id.avatar_view, bitmap);
                appWidgetManager.partiallyUpdateAppWidget(allWidgetIds, finalRemoteViews);
            });

            //If user click on life and xp: open the app
            Intent openAppIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            PendingIntent openApp = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_main_view, openApp);


            if (Build.VERSION.SDK_INT >= 16) {
                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                remoteViews = sizeRemoteViews(context, options, widgetId);
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
